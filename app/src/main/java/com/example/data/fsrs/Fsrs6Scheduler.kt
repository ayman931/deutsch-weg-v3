package com.example.data.fsrs

import com.example.data.model.LearningStatus
import com.example.data.model.ReviewLog
import com.example.data.model.ReviewState
import com.example.data.repository.ReviewRating
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Official FSRS-6 (Free Spaced Repetition Scheduler, Version 6) Implementation.
 * Reference: Open Spaced Repetition (open-spaced-repetition/FSRS-Kotlin and awesome-fsrs).
 * Uses the 21 optimized model parameters and the power-law forgetting curve with trainable decay w[20].
 */
class Fsrs6Scheduler(
    override val desiredRetention: Double = 0.90,
    override val parameters: DoubleArray = DEFAULT_PARAMETERS,
    val maxIntervalDays: Int = 36500
) : FsrsScheduler {

    companion object {
        /**
         * Global default 21 parameters for FSRS-6 from open-spaced-repetition.
         */
        val DEFAULT_PARAMETERS: DoubleArray = doubleArrayOf(
            0.212, 1.2931, 2.3065, 8.2956, 6.4133,
            0.8334, 3.0194, 0.001, 1.8722, 0.1666,
            0.796, 1.4835, 0.0614, 0.2629, 1.6483,
            0.6014, 1.8729, 0.5425, 0.0912, 0.0658,
            0.1542
        )

        private const val DAY_IN_MS = 86_400_000L
    }

    init {
        require(parameters.size == 21) {
            "FSRS-6 requires exactly 21 parameters, but received ${parameters.size}"
        }
        require(desiredRetention in 0.70..0.99) {
            "Desired retention must be between 0.70 and 0.99, received $desiredRetention"
        }
    }

    // Decay factor so R(S, S) = 0.90: factor = 0.9^(-1 / w[20]) - 1
    private val decayFactor: Double = 0.9.pow(-1.0 / parameters[20]) - 1.0

    /**
     * Rating grade integer mapping:
     * AGAIN = 1
     * HARD = 2
     * GOOD = 3
     * EASY = 4
     */
    fun gradeOf(rating: ReviewRating): Int = when (rating) {
        ReviewRating.AGAIN -> 1
        ReviewRating.HARD -> 2
        ReviewRating.GOOD -> 3
        ReviewRating.EASY -> 4
    }

    /**
     * Initial Stability S_0(G) = w[G - 1]
     */
    fun initStability(grade: Int): Double {
        return parameters[grade - 1]
    }

    /**
     * Initial Difficulty D_0(G) = clamp(w[4] - exp(w[5] * (G - 1)) + 1, 1.0, 10.0)
     */
    fun initDifficulty(grade: Int): Double {
        val raw = parameters[4] - exp(parameters[5] * (grade - 1.0)) + 1.0
        return raw.coerceIn(1.0, 10.0)
    }

    /**
     * Next Difficulty D''(D, G):
     * Delta D = -w[6] * (G - 3)
     * D' = D + Delta D * (10 - D) / 9
     * D'' = clamp(w[7] * D_0(4) + (1 - w[7]) * D', 1.0, 10.0)
     */
    fun nextDifficulty(d: Double, grade: Int): Double {
        val deltaD = -parameters[6] * (grade - 3.0)
        val dPrime = d + deltaD * ((10.0 - d) / 9.0)
        val d0Easy = initDifficulty(4)
        val dDoublePrime = parameters[7] * d0Easy + (1.0 - parameters[7]) * dPrime
        return dDoublePrime.coerceIn(1.0, 10.0)
    }

    /**
     * Retrievability R(t, S) = (1 + factor * (t / S))^(-w[20])
     */
    fun retrievability(elapsedDays: Double, stability: Double): Double {
        if (elapsedDays <= 0.0 || stability <= 0.0) return 1.0
        val base = 1.0 + decayFactor * (elapsedDays / stability)
        return base.pow(-parameters[20]).coerceIn(0.0, 1.0)
    }

    /**
     * Next interval given desired retention R_d:
     * I(R_d, S) = S * (R_d^(-1 / w[20]) - 1) / factor
     * When R_d = 0.90, this simplifies exactly to I = S.
     */
    fun nextInterval(stability: Double): Int {
        if (stability <= 0.0) return 1
        val numerator = desiredRetention.pow(-1.0 / parameters[20]) - 1.0
        val intervalDays = stability * (numerator / decayFactor)
        return intervalDays.roundToInt().coerceIn(1, maxIntervalDays)
    }

    /**
     * Next stability for successful recall (Grade in 2, 3, 4):
     * If elapsedDays == 0: short-term same-day formula
     * S' = S * exp(w[17] * (G - 3 + w[18])) * S^(-w[19])
     *
     * If elapsedDays > 0:
     * Delta S = 1 + exp(w[8]) * (11 - D) * S^(-w[9]) * (exp(w[10] * (1 - R)) - 1) * h * b
     * S' = S * Delta S
     */
    fun nextRecallStability(
        d: Double,
        s: Double,
        r: Double,
        grade: Int,
        elapsedDays: Double
    ): Double {
        if (elapsedDays < 1.0) {
            val sInc = exp(parameters[17] * (grade - 3.0 + parameters[18])) * s.pow(-parameters[19])
            return (s * sInc).coerceAtLeast(0.1)
        }

        val td = 11.0 - d
        val ts = s.pow(-parameters[9])
        val tr = exp(parameters[10] * (1.0 - r)) - 1.0
        val h = if (grade == 2) parameters[15] else 1.0
        val b = if (grade == 4) parameters[16] else 1.0
        val c = exp(parameters[8])

        val deltaS = 1.0 + c * td * ts * tr * h * b
        return (s * deltaS).coerceAtLeast(0.1)
    }

    /**
     * Next stability for forgetting / lapse (Grade == 1, Again):
     * d_f = D^(-w[12])
     * s_f = (S + 1)^w[13] - 1
     * r_f = exp(w[14] * (1 - R))
     * c_f = w[11]
     * S'_f = min(c_f * d_f * s_f * r_f, S)
     */
    fun nextForgetStability(
        d: Double,
        s: Double,
        r: Double
    ): Double {
        val df = d.pow(-parameters[12])
        val sf = (s + 1.0).pow(parameters[13]) - 1.0
        val rf = exp(parameters[14] * (1.0 - r))
        val cf = parameters[11]

        val raw = cf * df * sf * rf
        return min(raw, s).coerceAtLeast(0.1)
    }

    override fun schedule(
        currentState: ReviewState,
        rating: ReviewRating,
        now: Long
    ): FsrsScheduleResult {
        val grade = gradeOf(rating)
        val isFirstReview = currentState.status == LearningStatus.NEW || currentState.stability <= 0.0

        val elapsedDays: Double = if (currentState.lastReviewedDate != null) {
            max(0.0, ((now - currentState.lastReviewedDate) / DAY_IN_MS).toDouble())
        } else {
            0.0
        }

        val r = if (!isFirstReview) {
            retrievability(elapsedDays, currentState.stability)
        } else {
            1.0
        }

        val newDifficulty: Double
        val newStability: Double
        val newStatus: LearningStatus
        val newLapses: Int

        if (isFirstReview) {
            newDifficulty = initDifficulty(grade)
            newStability = initStability(grade)
            if (currentState.status == LearningStatus.REVIEW || currentState.status == LearningStatus.MASTERED) {
                if (grade == 1) {
                    newLapses = currentState.lapses + 1
                    newStatus = LearningStatus.RELEARNING
                } else {
                    newLapses = currentState.lapses
                    newStatus = LearningStatus.REVIEW
                }
            } else {
                newLapses = if (grade == 1) 1 else 0
                newStatus = when (rating) {
                    ReviewRating.AGAIN -> LearningStatus.LEARNING
                    ReviewRating.HARD -> LearningStatus.LEARNING
                    ReviewRating.GOOD -> LearningStatus.LEARNING
                    ReviewRating.EASY -> LearningStatus.REVIEW
                }
            }
        } else {
            newDifficulty = nextDifficulty(currentState.difficulty, grade)

            if (grade == 1) {
                // Lapse / Forget
                newStability = nextForgetStability(currentState.difficulty, currentState.stability, r)
                newLapses = currentState.lapses + 1
                newStatus = LearningStatus.RELEARNING
            } else {
                // Successful Recall
                newStability = nextRecallStability(currentState.difficulty, currentState.stability, r, grade, elapsedDays)
                newLapses = currentState.lapses
                newStatus = when (currentState.status) {
                    LearningStatus.NEW -> LearningStatus.LEARNING
                    LearningStatus.LEARNING -> LearningStatus.REVIEW
                    LearningStatus.RELEARNING -> LearningStatus.REVIEW
                    LearningStatus.REVIEW -> LearningStatus.REVIEW
                    LearningStatus.MASTERED -> LearningStatus.REVIEW
                }
            }
        }

        val intervalDays = nextInterval(newStability)
        val nextReviewTimestamp = now + (intervalDays * DAY_IN_MS)

        val updatedState = currentState.copy(
            status = newStatus,
            repetitionCount = currentState.repetitionCount + 1,
            lapses = newLapses,
            difficulty = newDifficulty,
            stability = newStability,
            intervalDays = intervalDays,
            nextReviewDate = nextReviewTimestamp,
            lastReviewedDate = now
        )

        val log = ReviewLog(
            reviewStateId = currentState.id,
            rating = rating,
            status = currentState.status,
            difficulty = newDifficulty,
            stability = newStability,
            elapsedDays = elapsedDays.roundToInt(),
            scheduledDays = intervalDays,
            reviewTimestamp = now
        )

        return FsrsScheduleResult(
            updatedState = updatedState,
            log = log,
            retrievability = r
        )
    }
}

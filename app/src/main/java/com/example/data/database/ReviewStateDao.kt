package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewLog
import com.example.data.model.ReviewState
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewStateDao {
    @Query("SELECT * FROM review_states WHERE wordId = :wordId")
    suspend fun getReviewStatesForWord(wordId: Long): List<ReviewState>

    @Query("SELECT * FROM review_states WHERE nextReviewDate <= :maxTimestamp ORDER BY nextReviewDate ASC")
    fun getDueReviewStates(maxTimestamp: Long): Flow<List<ReviewState>>

    @Query("SELECT * FROM review_states WHERE nextReviewDate <= :maxTimestamp AND direction = :direction ORDER BY nextReviewDate ASC")
    fun getDueReviewStatesByDirection(maxTimestamp: Long, direction: ReviewDirection): Flow<List<ReviewState>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewState(state: ReviewState): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewStates(states: List<ReviewState>): List<Long>

    @Update
    suspend fun updateReviewState(state: ReviewState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: ReviewLog): Long

    @Query("SELECT * FROM review_logs WHERE reviewStateId = :reviewStateId ORDER BY reviewTimestamp DESC")
    suspend fun getLogsForReviewState(reviewStateId: Long): List<ReviewLog>

    @Query("""
        SELECT COUNT(*) FROM review_states 
        WHERE CASE 
            WHEN (status = 'REVIEW' OR status = 'MASTERED') AND intervalDays >= 21 THEN 'MASTERED'
            WHEN status = 'MASTERED' AND intervalDays < 21 THEN 'REVIEW'
            ELSE status 
        END = :status
    """)
    fun countByStatus(status: LearningStatus): Flow<Int>
}

package id.makbarf.eatonomy.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HouseholdMemberDao {
    @Query("SELECT * FROM household_members ORDER BY name ASC")
    fun getAllMembers(): LiveData<List<HouseholdMember>>

    @Query("SELECT * FROM household_members WHERE id = :id")
    fun getMemberById(id: Int): LiveData<HouseholdMember>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: HouseholdMember)

    @Update
    suspend fun update(member: HouseholdMember)

    @Delete
    suspend fun delete(member: HouseholdMember)
} 
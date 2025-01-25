package id.makbarf.eatonomy.data

import androidx.lifecycle.LiveData

class HouseholdMemberRepository(private val householdMemberDao: HouseholdMemberDao) {
    val allMembers: LiveData<List<HouseholdMember>> = householdMemberDao.getAllMembers()

    fun getMemberById(id: Int): LiveData<HouseholdMember> {
        return householdMemberDao.getMemberById(id)
    }

    suspend fun insert(member: HouseholdMember) {
        householdMemberDao.insert(member)
    }

    suspend fun update(member: HouseholdMember) {
        householdMemberDao.update(member)
    }

    suspend fun delete(member: HouseholdMember) {
        householdMemberDao.delete(member)
    }
} 
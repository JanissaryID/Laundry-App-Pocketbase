package com.aluma.laundry.data.room.machine

import kotlinx.coroutines.flow.Flow

class MachineRepository(private val dao: MachineDAO) {
    val machineRoom: Flow<List<MachineRoom>> = dao.getAllMachine()

    suspend fun addMachine(machineRoom: MachineRoom) = dao.insert(machineRoom)
    suspend fun deleteMachine(machineRoom: MachineRoom) = dao.delete(machineRoom)
    suspend fun updateMachine(machineRoom: MachineRoom) = dao.update(machineRoom)
    suspend fun getMachineById(id: String): MachineRoom? = dao.getMachineById(id)
}
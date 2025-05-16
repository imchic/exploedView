package com.example.exploedview.db

import androidx.room.Dao

@Dao
interface BuildingInfoDao {

    // getMaxSeq
    @androidx.room.Query("SELECT MAX(seq) FROM building")
    fun getMaxSeq(): Int?

    @androidx.room.Query("SELECT * FROM building ORDER BY seq DESC")
    fun getAll(): List<BuildingInfo>

    @androidx.room.Query("SELECT * FROM building WHERE seq = :seq")
    fun getBySeq(seq: Int): BuildingInfo?

    @androidx.room.Query("SELECT * FROM building WHERE complex_pk = :complexPk")
    fun getBuildingInfoByComplexPk(complexPk: String): BuildingInfo?

    @androidx.room.Insert
    fun insert(buildingInfo: BuildingInfo): Long

    @androidx.room.Update
    fun update(buildingInfo: BuildingInfo): Int

    // 전체삭제
    @androidx.room.Query("DELETE FROM building")
    fun deleteAll()

    // delete by seq
    @androidx.room.Query("DELETE FROM building WHERE seq = :seq")
    fun deleteBySeq(seq: Int): Int

    // seq update
    @androidx.room.Query("UPDATE building SET filename = :complexPk WHERE seq = :seq")
    fun updateSeqByComplexPk(seq: Int, complexPk: String): Int

}
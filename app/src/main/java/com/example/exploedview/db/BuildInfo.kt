package com.example.exploedview.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building")
data class BuildingInfo(

    @PrimaryKey(autoGenerate = true) val seq: Int? = null, // 자동 증가 기본 키

    @ColumnInfo(name = "address") val address: String, // 주소

    @ColumnInfo(name = "complex_pk") val complexPk: String, // 단지 고유 키

    @ColumnInfo(name = "complex_nm1") val complexNm1: String, // 단지명 1

    @ColumnInfo(name = "complex_nm2") val complexNm2: String = "", // 단지명 2 (기본값: 빈 문자열)

    @ColumnInfo(name = "complex_nm3") val complexNm3: String = "", // 단지명 3 (기본값: 빈 문자열)

    @ColumnInfo(name = "complex_gb_cd") val complexGbCd: String, // 단지 구분 코드

    @ColumnInfo(name = "dong_cnt") val dongCnt: String, // 동 수

    @ColumnInfo(name = "unit_cnt") val unitCnt: String, // 세대 수

    @ColumnInfo(name = "useapr_dt") val useaprDt: String, // 사용 승인일

    // latitude
    @ColumnInfo(name = "latitude") val latitude: Double, // 위도

    // longitude
    @ColumnInfo(name = "longitude") val longitude: Double, // 경도

    // filename
    @ColumnInfo(name = "filename") val filename: String, // 파일 이름
)
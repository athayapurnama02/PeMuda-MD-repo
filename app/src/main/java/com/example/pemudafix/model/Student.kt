package com.example.pemudafix.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    var uid : String? = null,
    var details: Details? = null,
):Parcelable

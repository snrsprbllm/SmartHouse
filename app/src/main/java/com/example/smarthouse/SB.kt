package com.example.smarthouse

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable

object SB {
    private val supabaseUrl = "https://afgslxlqfpbpctunvood.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFmZ3NseGxxZnBicGN0dW52b29kIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzMxMjQzMzMsImV4cCI6MjA0ODcwMDMzM30.FctIeU0yl4iOYEmdi-Snx5h41-IC6IRQc_7KY_KJulw"

    private val supabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    fun getSb(): SupabaseClient {
        return supabaseClient
    }

    @Serializable
    data class User(
        val id: String,
        val username: String,
        val address: String?
    )

    @Serializable
    data class Home(
        val id: Int, // Убедитесь, что id может быть null
        val user_id: String,
        val address: String
    )

    @Serializable
    data class DeviceType(
        val id: Int,
        val name: String,
        val image_path: String?
    )

    @Serializable
    data class RoomType(
        val id: Int,
        val name: String,
        val image_path: String?
    )

    @Serializable
    data class Device(
        val id: Int,
        val device_id: String,
        val name: String,
        val device_type: Int,
        val room_id: Int,
        val home_id: Int, // Added this field
        val ison: Boolean,
        val value: Int
    )

    @Serializable
    data class Room(
        val id: Int,
        val name: String,
        val room_type: Int,
        val home_id: Int,
        val image_path: String? // Added this field
    )
}
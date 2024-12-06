package com.example.smarthouse

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

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
    public fun getSb(): SupabaseClient {
        return supabaseClient
    }
}
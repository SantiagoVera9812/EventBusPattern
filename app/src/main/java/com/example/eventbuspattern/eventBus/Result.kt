package com.example.eventbuspattern.eventBus

data class Result(val sportsKey: Int, val sportsName: String, val result: List<String>?, val isWARNING: Boolean = false) {
}
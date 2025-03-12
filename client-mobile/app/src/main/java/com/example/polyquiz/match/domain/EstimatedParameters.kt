package com.example.polyquiz.match.domain

data class EstimatedParameters (
    var lowerBound : Int,
    var upperBound : Int,
    var margin : Int,
    var correctAnswer: Int,
)

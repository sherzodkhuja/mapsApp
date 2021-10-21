package com.example.mapapp.models.place

data class Prediction(
    val description: String,
    val matched_substrings: List<MatchedSubstring>,
    val place_id: String,
    val reference: String,
    val structured_formatting: StructuredFormatting,
    val terms: List<Term>,
    val types: List<String>
)
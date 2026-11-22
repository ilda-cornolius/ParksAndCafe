package com.parksandcafe.app

enum class PlaceType(val firestoreValue: String) {
    PARK("park"),
    CAFE("cafe");

    companion object {
        fun fromAction(action: String?): PlaceType? = when (action) {
            WidgetActions.ACTION_SAVE_PARK -> PARK
            WidgetActions.ACTION_SAVE_CAFE -> CAFE
            else -> null
        }
    }
}

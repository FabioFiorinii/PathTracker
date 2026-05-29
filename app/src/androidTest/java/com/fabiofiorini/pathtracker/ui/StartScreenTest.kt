package com.fabiofiorini.pathtracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.fabiofiorini.pathtracker.MainActivity
import org.junit.Rule
import org.junit.Test

class StartScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun startScreenShowsBothButtons() {
        composeRule.onNodeWithText("Registra nuovo percorso").assertIsDisplayed()
        composeRule.onNodeWithText("Storico percorsi").assertIsDisplayed()
    }
}

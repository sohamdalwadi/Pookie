package com.pookie.game.presentation.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PookieFeatureUiTest {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ServiceLocator.initialize(context)
        ServiceLocator.isMockMode = true
        // Set up test profile
        ServiceLocator.preferencesRepository.saveBackendDeploymentId("AKfycb_MOCK_TEST_DEPLOYMENT_ID_FOR_UI_TEST")
        val profile = ServiceLocator.preferencesRepository.getCoupleProfile()
        ServiceLocator.preferencesRepository.saveCoupleProfile(
            profile.copy(myNickname = "Tester", partnerNickname = "Partner")
        )
    }

    @Test
    fun testLobbyScreenDisplaysAllGames() {
        ActivityScenario.launch(LobbyActivity::class.java).use {
            onView(withId(R.id.cardGameTicTacToe)).check(matches(isDisplayed()))
            onView(withId(R.id.cardGameDotsBoxes)).check(matches(isDisplayed()))
            onView(withId(R.id.cardGameQuiz)).check(matches(isDisplayed()))
            onView(withId(R.id.cardGameSoulSync)).check(matches(isDisplayed()))
            onView(withId(R.id.editRoomCode)).check(matches(isDisplayed()))
            onView(withId(R.id.btnJoinRoom)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testRoomCodeInputAndJoin() {
        ActivityScenario.launch(LobbyActivity::class.java).use {
            onView(withId(R.id.editRoomCode)).perform(clearText(), typeText("7J2L"), closeSoftKeyboard())
            onView(withId(R.id.btnJoinRoom)).perform(click())
        }
    }

    @Test
    fun testProfileSetupNicknameRoll() {
        ActivityScenario.launch(ProfileSetupActivity::class.java).use {
            onView(withId(R.id.btnDiceNickname)).perform(click())
            onView(withId(R.id.btnGenderFemale)).perform(click())
            onView(withId(R.id.btnGenderMale)).perform(click())
            onView(withId(R.id.btnSaveProfile)).check(matches(isDisplayed()))
        }
    }
}

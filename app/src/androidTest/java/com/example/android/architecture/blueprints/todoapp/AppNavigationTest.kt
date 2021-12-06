package com.example.android.architecture.blueprints.todoapp

import android.app.Activity
import android.view.Gravity
import android.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author Kulbaka Nataly
 * @date 06.12.2021
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {

    private lateinit var tasksRepository: TasksRepository

    // An Idling Resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        tasksRepository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your idling resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun tasksScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check that left drawer is closed at startup
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer should be closed.

        // Open drawer by clicking drawer icon
        Espresso.onView(
                ViewMatchers.withContentDescription(
                        activityScenario
                                .getToolbarNavigationContentDescription()
                )
        ).perform(ViewActions.click())

        // Check if drawer is open
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isOpen(Gravity.START))) // Left drawer is open open.
        // When using ActivityScenario.launch, always call close()
        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleUpButton() = runBlocking {
        val task = Task("Up <- button", "Description")
        tasksRepository.saveTask(task)

        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        Espresso.onView(ViewMatchers.withText("Up <- button")).perform(ViewActions.click())

        // Click on the edit task button
        Espresso.onView(ViewMatchers.withId(R.id.edit_task_fab)).perform(ViewActions.click())

        // Confirm that if we click up button once, we end up back at the task details page
        Espresso.onView(
                ViewMatchers.withContentDescription(
                        activityScenario
                                .getToolbarNavigationContentDescription()
                )
        ).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.task_detail_title_text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Confirm that if we click up button a second time, we end up back at the home screen
        Espresso.onView(
                ViewMatchers.withContentDescription(
                        activityScenario
                                .getToolbarNavigationContentDescription()
                )
        ).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tasks_container_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // When using ActivityScenario.launch, always call close()
        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleBackButton() = runBlocking {
        val task = Task("Back button", "Description")
        tasksRepository.saveTask(task)

        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        Espresso.onView(ViewMatchers.withText("Back button")).perform(ViewActions.click())

        // Click on the edit task button
        Espresso.onView(ViewMatchers.withId(R.id.edit_task_fab)).perform(ViewActions.click())

        // Confirm that if we click back once, we end up back at the task details page
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(R.id.task_detail_title_text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Confirm that if we click back a second time, we end up back at the home screen
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(R.id.tasks_container_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // When using ActivityScenario.launch, always call close()
        activityScenario.close()
    }
}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
                it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}
package com.oscarliang.knews.ui.bookmarks

import androidx.databinding.DataBindingComponent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentBindingAdapters
import com.oscarliang.knews.model.News
import com.oscarliang.knews.util.CountingAppExecutorsRule
import com.oscarliang.knews.util.DataBindingIdlingResourceRule
import com.oscarliang.knews.util.RecyclerViewMatcher
import com.oscarliang.knews.util.TestUtil
import com.oscarliang.knews.util.ViewModelUtil
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class BookmarksFragmentTest {

    @Rule
    @JvmField
    val countingAppExecutors = CountingAppExecutorsRule()

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule<BookmarksFragment>()

    private val newsLiveData = MutableLiveData<List<News>>()
    private lateinit var navController: NavController
    private lateinit var viewModel: BookmarksViewModel
    private lateinit var mockBindingAdapter: FragmentBindingAdapters

    @Before
    fun init() {
        navController = mock(NavController::class.java)
        viewModel = mock(BookmarksViewModel::class.java)
        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)
        `when`(viewModel.bookmarks).thenReturn(newsLiveData)
        val scenario = launchFragmentInContainer {
            BookmarksFragment().apply {
                appExecutors = countingAppExecutors.appExecutors
                viewModelFactory = ViewModelUtil.createFor(viewModel)
                dataBindingComponent = object : DataBindingComponent {
                    override fun getFragmentBindingAdapters(): FragmentBindingAdapters {
                        return mockBindingAdapter
                    }
                }
            }
        }
        dataBindingIdlingResourceRule.monitorFragment(scenario)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun testLoaded() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(listOf(news))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun clickNews() {
        val news = TestUtil.createNewsWithId(1, "foo", "a", "b", "c")
        newsLiveData.postValue(listOf(news))
        onView(withText("foo")).perform(click())
        verify(navController).navigate(BookmarksFragmentDirections.actionToNewsDetailFragment(1))
    }

    @Test
    fun clickBookmark() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(listOf(news))
        onView(withId(R.id.btn_bookmark)).perform(click())
        verify(viewModel).toggleBookmark(news)
    }

    @Test
    fun nullNews() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(listOf(news))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
        newsLiveData.postValue(null)
        onView(listMatcher().atPosition(0)).check(ViewAssertions.doesNotExist())
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.news_list)
    }

}
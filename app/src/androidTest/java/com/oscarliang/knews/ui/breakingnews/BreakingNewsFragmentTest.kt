package com.oscarliang.knews.ui.breakingnews

import androidx.databinding.DataBindingComponent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentBindingAdapters
import com.oscarliang.knews.model.News
import com.oscarliang.knews.util.CountingAppExecutorsRule
import com.oscarliang.knews.util.DataBindingIdlingResourceRule
import com.oscarliang.knews.util.RecyclerViewMatcher
import com.oscarliang.knews.util.Resource
import com.oscarliang.knews.util.TestUtil
import com.oscarliang.knews.util.ViewModelUtil
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class BreakingNewsFragmentTest {

    @Rule
    @JvmField
    val countingAppExecutors = CountingAppExecutorsRule()

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule<BreakingNewsFragment>()

    private val newsLiveData = MutableLiveData<Resource<List<News>>>()
    private lateinit var navController: NavController
    private lateinit var viewModel: BreakingNewsViewModel
    private lateinit var mockBindingAdapter: FragmentBindingAdapters

    @Before
    fun init() {
        navController = mock(NavController::class.java)
        viewModel = mock(BreakingNewsViewModel::class.java)
        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)
        doNothing().`when`(viewModel).setQuery(anyString(), anyString(), anyString(), anyInt())
        `when`(viewModel.news).thenReturn(newsLiveData)
        val scenario = launchFragmentInContainer {
            BreakingNewsFragment().apply {
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
    fun testLoading() {
        newsLiveData.postValue(Resource.loading(null))
        onView(withId(R.id.shimmer_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun testValueWhileLoading() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(Resource.loading(listOf(news)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun testLoaded() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(Resource.success(listOf(news)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun testError() {
        newsLiveData.postValue(Resource.error("Failed to load", null))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.btn_retry)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_retry)).perform(click())
        verify(viewModel).refresh()
        newsLiveData.postValue(Resource.loading(null))

        onView(withId(R.id.shimmer_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_retry)).check(matches(not(isDisplayed())))
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(Resource.success(listOf(news)))

        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.btn_retry)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun testRefresh() {
        newsLiveData.postValue(Resource.error("Failed to load", null))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipe_refresh_layout)).perform(swipeDown())
        verify(viewModel).refresh()
        newsLiveData.postValue(Resource.loading(null))

        onView(withId(R.id.shimmer_layout)).check(matches(isDisplayed()))
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(Resource.success(listOf(news)))

        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun nullBreakingNews() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(Resource.success(listOf(news)))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
        newsLiveData.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    @Test
    fun clickBreakingNews() {
        val news = TestUtil.createNewsWithId(1, "foo", "a", "b", "c")
        newsLiveData.postValue(Resource.success(listOf(news)))
        onView(withText("foo")).perform(click())
        verify(navController).navigate(BreakingNewsFragmentDirections.actionToNewsDetailFragment(1))
    }

    @Test
    fun clickBookmark() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(Resource.success(listOf(news)))
        onView(withId(R.id.btn_bookmark)).perform(click())
        verify(viewModel).toggleBookmark(news)
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.news_list)
    }

}
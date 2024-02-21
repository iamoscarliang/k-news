package com.oscarliang.knews.ui.search

import android.view.KeyEvent
import androidx.databinding.DataBindingComponent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentBindingAdapters
import com.oscarliang.knews.model.News
import com.oscarliang.knews.util.CountingAppExecutorsRule
import com.oscarliang.knews.util.DataBindingIdlingResourceRule
import com.oscarliang.knews.util.EspressoTestUtil.nestedScrollTo
import com.oscarliang.knews.util.RecyclerViewMatcher
import com.oscarliang.knews.util.Resource
import com.oscarliang.knews.util.TestUtil
import com.oscarliang.knews.util.ViewModelUtil
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq

@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    @Rule
    @JvmField
    val countingAppExecutors = CountingAppExecutorsRule()

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule<SearchFragment>()

    private val searchResults = MutableLiveData<Resource<List<News>>>()
    private lateinit var navController: NavController
    private lateinit var viewModel: SearchViewModel
    private lateinit var mockBindingAdapter: FragmentBindingAdapters
    private val loadMoreStatus = MutableLiveData<SearchViewModel.LoadMoreState>()

    @Before
    fun init() {
        navController = mock(NavController::class.java)
        viewModel = mock(SearchViewModel::class.java)
        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)
        doReturn(loadMoreStatus).`when`(viewModel).loadMoreStatus
        `when`(viewModel.searchResults).thenReturn(searchResults)
        val scenario = launchFragmentInContainer(themeResId = R.style.Theme_KNews) {
            SearchFragment().apply {
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
    fun search() {
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_search)).perform(
            typeText("foo"),
            pressKey(KeyEvent.KEYCODE_ENTER)
        )
        verify(viewModel).setQuery(eq("foo"), anyString(), anyString(), anyString(), anyInt())
        searchResults.postValue(Resource.loading(null))
        onView(withId(R.id.shimmer_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun loadResults() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        searchResults.postValue(Resource.success(listOf(news)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun dataWithLoading() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        searchResults.postValue(Resource.loading(listOf(news)))
        onView(withId(R.id.shimmer_layout)).check(matches(not(isDisplayed())))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo"))))
    }

    @Test
    fun error() {
        searchResults.postValue(Resource.error("Failed to load", null))
        onView(withId(R.id.text_error)).check(matches(isDisplayed()))
    }

    @Test
    fun loadMore() {
        val news = TestUtil.createNews(10, "foo", "a", "b", "c")
        searchResults.postValue(Resource.success(news))
        onView(listMatcher().atPosition(9)).perform(nestedScrollTo())
        onView(listMatcher().atPosition(9)).check(matches(isDisplayed()))
        verify(viewModel).loadNextPage()
    }

    @Test
    fun navigateToNews() {
        doNothing().`when`(viewModel).loadNextPage()
        val news = TestUtil.createNewsWithId(1, "foo", "a", "b", "c")
        searchResults.postValue(Resource.success(listOf(news)))
        onView(withText("foo")).perform(click())
        verify(navController).navigate(SearchFragmentDirections.actionToNewsDetailFragment(1))
    }

    @Test
    fun loadMoreProgress() {
        loadMoreStatus.postValue(SearchViewModel.LoadMoreState(true, null))
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()))
        loadMoreStatus.postValue(SearchViewModel.LoadMoreState(false, null))
        onView(withId(R.id.progressbar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun loadMoreProgressError() {
        loadMoreStatus.postValue(SearchViewModel.LoadMoreState(true, "Failed to load"))
        onView(withText("Failed to load")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun clickBookmark() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        searchResults.postValue(Resource.success(listOf(news)))
        onView(withId(R.id.btn_bookmark)).perform(click())
        verify(viewModel).toggleBookmark(news)
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.news_list)
    }

}
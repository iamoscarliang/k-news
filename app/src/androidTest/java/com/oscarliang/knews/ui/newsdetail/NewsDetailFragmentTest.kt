package com.oscarliang.knews.ui.newsdetail

import androidx.databinding.DataBindingComponent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.oscarliang.knews.R
import com.oscarliang.knews.binding.FragmentBindingAdapters
import com.oscarliang.knews.model.News
import com.oscarliang.knews.util.DataBindingIdlingResourceRule
import com.oscarliang.knews.util.EspressoTestUtil.withCollapsibleToolbarTitle
import com.oscarliang.knews.util.TestUtil
import com.oscarliang.knews.util.ViewModelUtil
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class NewsDetailFragmentTest {

    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule<NewsDetailFragment>()

    private val newsLiveData = MutableLiveData<News>()
    private lateinit var navController: NavController
    private lateinit var viewModel: NewsDetailViewModel
    private lateinit var mockBindingAdapter: FragmentBindingAdapters

    @Before
    fun init() {
        navController = mock(NavController::class.java)
        viewModel = mock(NewsDetailViewModel::class.java)
        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)
        doNothing().`when`(viewModel).setNewsId(anyInt())
        `when`(viewModel.news).thenReturn(newsLiveData)
        val scenario = launchFragmentInContainer(
            fragmentArgs = NewsDetailFragmentArgs(newsId = 0).toBundle(),
            themeResId = R.style.Theme_KNews
        ) {
            NewsDetailFragment().apply {
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
        val news = TestUtil.createNews("foo", "", "bar", "buzz")
        newsLiveData.postValue(news)
        onView(isAssignableFrom(CollapsingToolbarLayout::class.java)).check(
            matches(
                withCollapsibleToolbarTitle(`is`("foo"))
            )
        )
        onView(withId(R.id.text_publish_date)).check(matches(withText("bar")))
        onView(withId(R.id.text_author)).check(matches(withText("buzz")))
    }

    @Test
    fun clickBookmark() {
        val news = TestUtil.createNews("foo", "a", "b", "c")
        newsLiveData.postValue(news)
        onView(withId(R.id.fab_bookmark)).perform(click())
        verify(viewModel).toggleBookmark(news)
    }

}
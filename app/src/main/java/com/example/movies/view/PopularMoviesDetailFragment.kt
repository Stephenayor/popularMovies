package com.example.movies.view

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movies.Constants.YOUTUBE_TRAILER_BASE_URL
import com.example.movies.R
import com.example.movies.adapter.TrailersAdapter
import com.example.movies.base.BaseFragment
import com.example.movies.databinding.FragmentMainBinding
import com.example.movies.databinding.FragmentPopularMoviesDetailBinding
import com.example.movies.model.Trailer
import com.example.movies.model.TrailersResult
import com.example.movies.viewmodel.MoviesViewModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PopularMoviesDetailFragment : BaseFragment(), TrailersAdapter.TrailerClickInterface {
    lateinit var binding: FragmentPopularMoviesDetailBinding
    lateinit var popularMovies: PopularMoviesDetailFragmentArgs
    @Inject
    lateinit var moviesViewModel: MoviesViewModel
    val trailerList: MutableLiveData<TrailersResult>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentPopularMoviesDetailBinding>(
                inflater, R.layout.fragment_popular_movies_detail, container,
                false
        )

        binding.root.setBackgroundColor(Color.WHITE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popularMovies = arguments?.let { PopularMoviesDetailFragmentArgs.fromBundle(it) }!!
        moviesViewModel = moviesComponent.getMoviesViewModel()

        Glide.with(this).load(
                "https://image.tmdb.org/t/p/w500/" +
                        popularMovies?.movies?.backdropPath
        )
                .into(binding.popularMoviesBackdropDetailsImageView)
        binding.popularMoviesDetailsTitleTextview.text = popularMovies?.movies?.title
        Glide.with(this).load("https://image.tmdb.org/t/p/w500/" +
                popularMovies?.movies?.posterPath)
                .into(binding.moviePoster)
        getTrailers()
    }

    private fun getTrailers() {
        moviesViewModel.getTrailers(popularMovies)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        object : SingleObserver<TrailersResult?> {
                            override fun onSubscribe(d: Disposable) {
                                Toast.makeText(context, "TRAILERS ARE COMING", Toast.LENGTH_SHORT)
                            }
                            override fun onSuccess(trailers: TrailersResult) {
                                generateMoviesTrailerList(trailers.results)
                            }
                            override fun onError(e: Throwable) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                            }
                        }
                )
    }

    private fun generateMoviesTrailerList(trailersList: List<Trailer>) {
        binding.trailersRecyclerView.setHasFixedSize(true)
        val trailersAdapter = TrailersAdapter(this)
        trailersAdapter.moviesTrailers = trailersList
        binding.trailersRecyclerView.adapter = trailersAdapter
        binding.trailersRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
        )
    }

    override fun onMovieTrailerClick(trailers: Trailer) {
        val openYoutubeIntent = Intent(Intent.ACTION_VIEW)
        openYoutubeIntent.data = Uri.parse(YOUTUBE_TRAILER_BASE_URL + trailers.key)
        startActivity(openYoutubeIntent)
    }

}
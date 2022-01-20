package com.example.roomdb_ekianoaf_09

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdb_ekianoaf_09.databinding.ActivityMainBinding
import com.example.roomdb_ekianoaf_09.room.Constant
import com.example.roomdb_ekianoaf_09.room.Movie
import com.example.roomdb_ekianoaf_09.room.MovieDatabase

import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {




    val db by lazy { MovieDatabase(this) }
    lateinit var movieAdapter: MovieAdapter

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListener()
        setupRecyclerView()

    }

    override fun onStart() {
        super.onStart()
        loadMovies()
    }

    fun loadMovies(){
        CoroutineScope(Dispatchers.IO).launch {
            val movies = db.movieDao().getMovies()
            Log.d("MainActivity","dbResponse: $movies")
            withContext(Dispatchers.Main){
                movieAdapter.setData(movies)
            }
        }
    }

    private fun setupListener() {
        binding.addMovie.setOnClickListener {
            intentEdit(0, Constant.TYPE_CREATE)
        }
    }

    fun intentEdit(movieID : Int, intentType : Int){
        startActivity(Intent(this@MainActivity, AddActivity::class.java)
            .putExtra("intent_id", movieID)
            .putExtra("intent_type", intentType)
        )
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(arrayListOf(), object : MovieAdapter.OnAdapterChangeListener{
            override fun onRead(movie: Movie) {
                intentEdit(movie.id, Constant.TYPE_READ)
            }

            override fun onUpdate(movie: Movie) {
                intentEdit(movie.id, Constant.TYPE_UPDATE)
            }

            override fun onDelete(movie: Movie) {
                deleteDialog(movie)
            }
        })
        binding.rvMovie.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = movieAdapter
        }
    }

    private fun deleteDialog(movie : Movie){
        val alertDeleteDialog = AlertDialog.Builder(this)
        alertDeleteDialog.apply {
            setTitle("Konfirmasi")
            setMessage("Yakin untuk menghapus ${movie.title}")
            setNegativeButton("Batal") { dialogInterface, i -> dialogInterface.dismiss() }
            setPositiveButton("Hapus") { dialogInterface, i ->
                dialogInterface.dismiss()
                CoroutineScope(Dispatchers.IO).launch {
                    db.movieDao().deleteMovie(movie)
                    loadMovies()
                }
            }
        }
        alertDeleteDialog.show()
    }
}
package edu.cnm.deepdive.skywatch.app.controller

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import edu.cnm.deepdive.skywatch.app.R
import edu.cnm.deepdive.skywatch.app.adapter.SearchResultsAdapter
import edu.cnm.deepdive.skywatch.app.databinding.ActivityMainBinding
import edu.cnm.deepdive.skywatch.app.viewmodel.SearchViewModel
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    private lateinit var adapter: SearchResultsAdapter

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupLayout()
    }

    private fun setupLayout() {
        enableEdgeToEdge()
        setContentView(binding.root) // TODO: Change to use view binding
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.searchResults
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        viewModel.searchResults.observe(this) { results ->
            adapter.clear()
            adapter.addAll(results)
        }
    }

    private fun setupListners(){
        binding
            .submit
            .setOnClickListener {
                val searchText: String = binding
                    .searchFilter
                    .text
                    .toString()
                    .trim()
                if (searchText.length > 0) {
                    viewModel.search(searchText, null, null) //TODO Add values from date/time
                }
            }
    }
}
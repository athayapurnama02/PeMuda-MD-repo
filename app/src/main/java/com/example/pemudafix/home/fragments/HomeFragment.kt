package com.example.pemudafix.home.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
// import com.google.firebase.auth.FirebaseAuth
import com.example.pemudafix.R
import com.example.pemudafix.databinding.FragmentHomeBinding
import com.example.pemudafix.home.activity.UserActivity
import com.example.pemudafix.home.adapter.JobListAdapter
import com.example.pemudafix.home.viewmodel.HomeViewModel
import com.example.pemudafix.model.Job
import com.example.pemudafix.util.Status.*
import com.example.pemudafix.util.counterAnimation
import com.example.pemudafix.util.getGreeting
import com.example.pemudafix.util.showToast
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _jobListAdapter: JobListAdapter? = null
    private val jobListAdapter get() = _jobListAdapter!!

    private val homeViewModel by viewModels<HomeViewModel>()
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        _jobListAdapter = JobListAdapter(::onItemClick, requireActivity())

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            homeViewModel.fetchMetrics()
            homeViewModel.fetchJobs()

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val username = mAuth.currentUser?.displayName!!
                    ivProfileImage.load(mAuth.currentUser?.photoUrl)
                }
            }

            ivProfileImage.setOnClickListener {
                navigateToUserActivity()
            }

            rvRecentJobs.adapter = jobListAdapter
            rvRecentJobs.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObserver() {
        homeViewModel.metrics.observe(viewLifecycleOwner) { metrics ->
            when (metrics.status) {
                LOADING -> Unit
                SUCCESS -> {
                    val (companiesCount, jobsAppliedCount) = metrics.data!!
                    counterAnimation(0, companiesCount, binding.tvCompaniesCount)
                    counterAnimation(0, jobsAppliedCount, binding.tvJobAppliedCount)
                }
                ERROR -> {
                    val errorMessage = metrics.message!!
                    showToast(requireContext(), errorMessage)
                }
            }
        }

        homeViewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            when (jobs.status) {
                LOADING -> Unit
                SUCCESS -> {
                    val jobList = jobs.data!!.take(3)
                    jobListAdapter.setJobListData(jobList)
                }
                ERROR -> {
                    val errorMessage = jobs.message!!
                    showToast(requireContext(), errorMessage)
                }
            }
        }
    }

    private fun navigateToUserActivity() {
        val userActivity = Intent(requireContext(), UserActivity::class.java)
        startActivity(userActivity)
    }

    private fun onItemClick(job: Job) {
        val direction = HomeFragmentDirections.actionHomeFragmentToJobViewActivity(job = job)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        _jobListAdapter = null
        _binding = null
        super.onDestroyView()
    }
}
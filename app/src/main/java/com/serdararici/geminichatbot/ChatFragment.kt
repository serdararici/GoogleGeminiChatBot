package com.serdararici.geminichatbot

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.serdararici.geminichatbot.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: ChatViewModel
    private var messageAdapter = MessageAdapter()
    private var messageList = mutableListOf<Pair<String, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val geminiAi = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = Constans.API_KEY
        )

        viewModel = ViewModelProvider(
            this,
            ChatViewModel.ChatViewModelFactory(geminiAi)
        ).get(ChatViewModel::class.java)

        setAdapter()
        sendMessage()
        observe()
    }

    private fun setAdapter(){
        val ll = LinearLayoutManager(requireContext())
        ll.stackFromEnd = true
        binding.rcChat.layoutManager = ll
        binding.rcChat.setHasFixedSize(true)
        binding.rcChat.adapter = messageAdapter
    }

    private fun sendMessage(){
        binding.ivSend.setOnClickListener {
            val userMessage = binding.evSend.text.toString().trim()
            viewModel.geminiChat(userMessage)
            messageList.add(Pair(userMessage,MessageAdapter.VIEW_TYPE_USER))
            messageAdapter.setMessages(messageList)
            scrollPosition()
            binding.evSend.setText("")
            binding.imagePromptProgress.visibility = View.VISIBLE
            val inptMethod = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inptMethod.hideSoftInputFromWindow(it.windowToken,0)
        }
    }

    private fun observe(){
        viewModel.messageResponse.observe(viewLifecycleOwner, Observer { content->
            content.text?.let {
                messageList.add(Pair(it,MessageAdapter.VIEW_TYPE_GEMINI))
                messageAdapter.setMessages(messageList)
                binding.imagePromptProgress.visibility = View.GONE
                scrollPosition()
            }
        })
    }

    private fun scrollPosition(){
        binding.rcChat.smoothScrollToPosition(messageAdapter.itemCount - 1)
    }
}
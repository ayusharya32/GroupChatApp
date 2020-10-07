package com.example.groupchat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment: Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_chatFragment)
        }

        clearErrorsOnTextChange()

        tvNew.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        btnLogin.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val email = etLoginEmail.text.trim().toString()
        val password = etLoginPassword.text.trim().toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            if (isEmailValid(email)) {
                pbLogin.visibility = View.VISIBLE
                loginUser(email, password)
            } else {
                tlLoginEmail.error = "Invalid Email"
                tlLoginEmail.requestFocus()
            }
        } else {
            setErrorsOnEditText(email, password)
        }
    }

    private fun loginUser(email: String, password: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), "Successfully Logged In", Toast.LENGTH_SHORT).show()
                pbLogin.visibility = View.INVISIBLE
                findNavController().navigate(R.id.action_loginFragment_to_chatFragment)
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                pbLogin.visibility = View.INVISIBLE
            }
        }
    }

    private fun isEmailValid(email: String): Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setErrorsOnEditText(email: String, password: String) {
        if(email.isEmpty()) {
            tlLoginEmail.error = "Email Required"
            tlLoginEmail.requestFocus()
        }
        if(password.isEmpty()){
            tlLoginPassword.error = "Password Required"
            tlLoginPassword.requestFocus()
        }
    }

    private fun clearErrorsOnTextChange() {
        val textWatcher = object: TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                tlLoginEmail.error = null
                tlLoginPassword.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/*NO-OP*/}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {/*NO-OP*/}
        }

        etLoginEmail.addTextChangedListener(textWatcher)
        etLoginPassword.addTextChangedListener(textWatcher)
    }
}
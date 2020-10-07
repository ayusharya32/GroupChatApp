package com.example.groupchat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterFragment: Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        clearErrorsOnTextChange()

        btnRegister.setOnClickListener {
            attemptRegistration()
        }
    }

    private fun attemptRegistration() {
        val email = etRegisterEmail.text.trim().toString()
        val password = etRegisterPassword.text.trim().toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            if(isEmailValid(email)){
                pbRegister.visibility = View.VISIBLE
                registerUser(email, password)
            } else {
                tlRegisterEmail.error = "Invalid Email"
                tlRegisterEmail.requestFocus()
            }
        } else {
            setRequiredErrorsOnEditText(email, password)
        }
    }

    private fun registerUser(email: String, password: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), "User Registered Successfully", Toast.LENGTH_SHORT).show()
                pbRegister.visibility = View.GONE
                findNavController().navigate(R.id.action_registerFragment_to_chatFragment)
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                pbRegister.visibility = View.GONE
            }
        }
    }

    private fun isEmailValid(email: String): Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setRequiredErrorsOnEditText(email: String, password: String) {
        if(email.isEmpty()) {
            tlRegisterEmail.error = "Email Required"
            etRegisterEmail.requestFocus()
        }
        if(password.isEmpty()){
            tlRegisterPassword.error = "Password Required"
            etRegisterPassword.requestFocus()
        }
    }

    private fun clearErrorsOnTextChange() {
        val textWatcher = object: TextWatcher{

            override fun afterTextChanged(s: Editable?) {
                tlRegisterEmail.error = null
                tlRegisterPassword.error = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/*NO-OP*/}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {/*NO-OP*/}
        }

        etRegisterEmail.addTextChangedListener(textWatcher)
        etRegisterPassword.addTextChangedListener(textWatcher)
    }
}
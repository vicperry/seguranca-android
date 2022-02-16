package com.cursoandroid.meulocalizador.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.cursoandroid.meulocalizador.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
            private lateinit var binding: ActivityLoginBinding

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                binding = ActivityLoginBinding.inflate(layoutInflater)
                val view = binding.root
                setContentView(view)

                binding.loginButton.setOnClickListener {
                    when {
                        TextUtils.isEmpty(
                            binding.emailTextInputEditText.text.toString().trim { it <= ' ' }) -> {
                            Toast.makeText(this, "Insira seu e-mail.", Toast.LENGTH_LONG).show()
                        }
                        TextUtils.isEmpty(
                            binding.senhaTextInputEditText.text.toString().trim { it <= ' ' }) -> {
                            Toast.makeText(this, "Insira sua senha.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            val email: String =
                                binding.emailTextInputEditText.text.toString().trim { it <= ' ' }
                            val senha: String =
                                binding.senhaTextInputEditText.text.toString().trim { it <= ' ' }

                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
                                .addOnCompleteListener(
                                    { task ->

                                        if (task.isSuccessful) {
                                            val firebaseUser: FirebaseUser = task.result!!.user!!

                                            Toast.makeText(
                                                this, "Login feito com sucesso.", Toast.LENGTH_LONG).show()

                                            val intent = Intent(this, MainActivity::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            intent.putExtra("user_id", firebaseUser.uid)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                task.exception!!.message.toString(),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                )
                        }
                    }
                }

                binding.cadastrarButton.setOnClickListener {
                    when {
                        TextUtils.isEmpty(
                            binding.emailTextInputEditText.text.toString().trim { it <= ' ' }) -> {
                            Toast.makeText(this, "Insira um e-mail válido.", Toast.LENGTH_LONG).show()
                        }
                        TextUtils.isEmpty(
                            binding.senhaTextInputEditText.text.toString().trim { it <= ' ' }) -> {
                            Toast.makeText(this, "Insira uma senha.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            val email: String =
                                binding.emailTextInputEditText.text.toString().trim { it <= ' ' }
                            val senha: String =
                                binding.senhaTextInputEditText.text.toString().trim { it <= ' ' }

                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
                                .addOnCompleteListener(
                                    { task ->

                                        if (task.isSuccessful) {
                                            val firebaseUser: FirebaseUser = task.result!!.user!!

                                            Toast.makeText(
                                                this, "Usuário cadastrado com sucesso.", Toast.LENGTH_LONG).show()

                                            val intent = Intent(this, MainActivity::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            intent.putExtra("user_id", firebaseUser.uid)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                task.exception!!.message.toString(),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                )
                        }
                    }
                }
            }
}
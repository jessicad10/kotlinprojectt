package com.example.petalsandbloom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petalsandbloom.model.UserModel
import com.example.petalsandbloom.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel (val repo: UserRepository): ViewModel() {


    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.login(email,password,callback)
    }

    //authentication function
    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    ){
        repo.register(email,password,callback)
    }

    //databasefunction
    fun addUserToDatabase(
        userId: String, model: UserModel
        , callback: (Boolean, String) -> Unit
    ){
        repo.addUserToDatabase(userId, model,callback)
    }

    fun updateProfile(
        userId: String, data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ){
        repo.updateProfile(userId,data,callback)
    }

    fun forgetPassword(
        email: String, callback: (Boolean, String) -> Unit

    ){
        repo.forgetPassword (email,callback)
    }


    fun getCurrentUser(): FirebaseUser?{
        return repo.getCurrentUser()
    }
    private val _users = MutableLiveData< UserModel?>()
    val users : LiveData<UserModel?> get() = _users
    fun getUserById(userId: String,)
    {
        repo.getUserById (userId){
                users,sucess,message->
            if (sucess && users != null){
                _users.postValue(users)
            }else{
                _users.postValue(null)
            }
        }
    }

    fun logout(
        callback: (Boolean, String) -> Unit
    ){
        repo.logout (callback)
    }
}



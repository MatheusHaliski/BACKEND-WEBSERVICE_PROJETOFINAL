package br.pucpr.authserver.users

data class UpdateProfileRequest(val email: String, val name: String, val profilePic: String)
'use strict'

const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
	username: {type: String, lowercase: true, unique: true, required: true},
	password: {type: String, required: true},
	gender: {type: String, required: true},
	age: {type: String, required: true},
	created_at: {type: Date},
});


module.exports = mongoose.model('User', UserSchema);
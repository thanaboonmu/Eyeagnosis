'use strict'

const config = require('../config/auth.json');
const jwt = require('jsonwebtoken');

const User = require('../models/user');
const bcrypt = require('bcryptjs');

exports.signup = (username, password, gender, age) => 
	
	new Promise((resolve, reject) => {
		const saltRounds = 10;
		const salt = bcrypt.genSaltSync(saltRounds);
		const hash = bcrypt.hashSync(password, salt);
		const user = new User({
			username: username,
			password: hash,
			gender: gender,
			age: age,
			created_at: new Date()
		});
		user.save()
		.then(() => resolve({status: 201, message: 'Signed up successfully'}))
		.catch(err => {
			if (err.code == 11000) { // duplicate key mongodb
				reject({status: 409, message: 'This user is duplicated'});
			} else {
				reject({status: 500, message: 'Internal server error'});
			}
		});

	});


exports.signin = (username, password) =>

	new Promise((resolve, reject) => {
		User.findOne({username: username})
		.then(user => {
			if (user) {
				return user;
			} else {
				reject({status: 404, message: 'User not found'});
			}
		})
		.then(user => {
			if (bcrypt.compareSync(password, user.password)) {
				resolve({ status: 200, message: username});
			} else {
				reject({status: 401, message: 'Invalid username or password'});
			}
		})
		.catch(err => reject({status: 500, message: 'Internal server error'}));
	});


exports.checkToken = (req) => {
		let token = req.headers['authorization'];
		let pattern = 'Bearer '
		if (token && token.startsWith(pattern)) {
			token = token.substring(pattern.length);
		}
		if (token) {
			try {
				var decoded = jwt.verify(token, config.secret);
				return decoded.message === req.params.id || decoded.message === req.query.username;
			} catch (err) {
				console.log("Couldn't decode the token");
				return false;
			}
		} else {
			console.log("No token found");
			return false;
		}
	}
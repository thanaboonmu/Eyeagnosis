'use strict'

const Result = require('../models/result');

exports.getResults = (username) =>
	new Promise((resolve, reject) => {
		Result.find({username: username})
		.then(result => {
			resolve(result);
		})
		.catch(err => {
			reject({status: 500, message: 'Internal server errror'});
		})
	});
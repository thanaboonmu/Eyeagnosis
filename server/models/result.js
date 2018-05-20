'use strict'

const mongoose = require('mongoose')

const ResultSchema = new mongoose.Schema({
	username: {type: String, required: true},
	imgUrl: {type: String, required: true},
	side: {type: String, required: true},
	disease: {type: String, required: true},
	possibility: {type: String, required: true},
	date: {type: Date, required: true}
});

module.exports = mongoose.model('Result', ResultSchema);


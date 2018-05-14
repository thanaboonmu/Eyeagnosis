'use strict'

const auth = require('basic-auth');
const jwt = require('jsonwebtoken');

const config = require('./config/auth.json');
const AuthenticationController = require('./controllers/authentication');
const ResultController = require('./controllers/results');
const UploadController = require('./controllers/upload');

module.exports = (router) => {

	router.post('/authenticate', (req, res) => {
		const credentials = auth(req);
		if (!credentials) {
			res.status(400).json({message: 'Invalid request'});
		} else {
			AuthenticationController.signin(credentials.name, credentials.pass)
			.then(result => {
				const token = jwt.sign(result, config.secret, {expiresIn: '365d'});
				res.status(result.status).json({message: result.message, token: token});
			})
			.catch(err => res.status(err.status).json({message: err.message}));
		}
	});

	router.post('/users', (req, res) => {
		const credentials = auth(req);
		const gender = req.body.gender;
		const age = req.body.age;
		if (!credentials) {
			res.status(400).json({message: 'Invalid request'});
		} 
		if (!gender || !age) {
			console.log("req.body",req.body);
			res.status(400).json({message: 'Invalid request'});
		} else {
			AuthenticationController.signup(credentials.name, credentials.pass, gender, age)
			.then(result => {
				res.status(result.status).json({message: result.message});
			})
			.catch(err => res.status(err.status).json({message: err.message}));
		}
	});

	router.get('/users/:id', (req,res) => {
		if (AuthenticationController.checkToken(req)) {
			ResultController.getResults(req.params.id)
			.then(result => res.json(result))
			.catch(err => res.status(err.status).json({message: err.message}));
		} else {
			res.status(401).json({message: 'Unauthorized'});
		}
	});

	router.post('/image', UploadController.upload);

} 

'use strict'

const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const logger = require('morgan');
const router = express.Router();

const mongoose = require('mongoose');
const dbconfig = require('./config/database.json');

mongoose.Promise = global.Promise;
mongoose.connect(dbconfig.url);

app.use(bodyParser.json());
app.use(logger('dev'));

require('./routes')(router);
app.use('/api', router);

app.listen(8080);
console.log('Running on http://localhost:8080/');
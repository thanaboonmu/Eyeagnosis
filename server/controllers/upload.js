'use strict'

const config = require('../config/config');
const express = require('express');
const gcloud = require('google-cloud');
const PythonShell = require('python-shell');
const multiparty = require('multiparty');
const fs = require('fs');


const storage = gcloud.storage({
projectId: config.projectId,
keyFilename: config.keyFilename
});

const bucket = storage.bucket(config.bucketName);

const AuthenticationController = require('./authentication');
const Result = require('../models/result');

function rawBody(req, res, next) {
    var chunks = [];
    // console.log(req);
    req.on('data', function(chunk) {
        chunks.push(chunk);
    });

    req.on('end', function() {
        var buffer = Buffer.concat(chunks);

        req.bodyLength = buffer.length;
        req.rawBody = buffer;
        next();
    });

    req.on('error', function (err) {
        console.log(err);
        res.status(500);
    });
}

function uploadToBucket(imageData, callback) {
    // Generate a unique filename for this image
    console.log("Uploading@ " + new Date());
    var filename = '' + new Date().getTime() + '-' + Math.random() + '.png';
    var file = bucket.file(filename);
    var imageUrl = 'https://' + config.bucketName + '.storage.googleapis.com/' + filename;
    var stream = file.createWriteStream({'metadata':{'contentType': 'image/png'}});
    stream.on('error', callback);
    stream.on('finish', function() {
      // Set this file to be publicly readable
      file.makePublic(function(err) {
        if (err) {
            callback(err, null);
        }
        else {
            callback(null, imageUrl);
        }
      });
    });
    stream.end(imageData);
  }

function runPython(imageUrl, mode, callback) {
    var options = {
      args: [imageUrl]
    };
    var filename = "normal_test.py"
    if (mode == "normal") {
        filename = "normal_test.py";
    } else if (mode == "red_reflect") {
        filename = "red_reflect_test.py";
    } else {
        filename = "normal_test.py";
    }
    PythonShell.run(filename, options, function (err, results) {
        if (err) {
            return callback(err, null);
        } else {
            console.log('Python job is done');
            console.log(results);
            callback(null, results);
        }
    });
}


function saveResultToDB(record, callback) {
    const result = new Result({
        username: record.username,
        side: record.result.side,
        disease: record.result.disease,
        possibility: record.result.possibility,
        date: new Date()
    });
    result.save(function(err){
        if(err){
            return callback(err,null);
        } else {
            return callback(null,"Record saved successfully");
        }
    });
}

exports.upload = (req, res) => {
    var form = new multiparty.Form();
    form.parse(req, function(err, fields, files) {
        Object.keys(fields).forEach(function(name) {
            console.log('got field named ' + name);
        });
        if (files && Object.keys(files).length > 0) {
            Object.keys(files).forEach(function(name) {
                console.log('got file named ' + name);
                var file = files[name][0];
                console.log(JSON.stringify(file));
                var raw = fs.readFileSync(file.path);
                // save image to bucket
                uploadToBucket(raw, function(err, imageUrl) {
                    if (err) {
                        res.status(500).send(err);
                    }
                    else {
                        console.log("Finish uploading@ " + new Date());
                        console.log('Uploaded to bucket: ' + imageUrl);
                        // call Python backend
                        runPython(imageUrl, req.query.mode, function(err, results) {
                            var response = {};
                            if (err) {
                                res.status(500).send(err);
                            }
                            else {
                                var result = null
                                if (results && results.length > 0) {
                                    result = JSON.parse(results[0].replace(new RegExp('\'', 'g'), '"'));
                                }
                                if (!result) {
                                    result = {};
                                }
                                result.side = req.query.side;
                                response.result = result;
                                response.status = 'OK';

                                if(req.query.username) {
                                    if(AuthenticationController.checkToken(req)) {
                                        response.username = req.query.username;
                                        saveResultToDB(response, function(err, success){
                                            if (err) {
                                                console.log(err);
                                            } else {
                                                console.log(success);
                                            }
                                        });
                                    } else {
                                        console.log("Unauthorized, didn't save the record");
                                    }
                                } else {
                                    console.log("No username found, didn't save the record");
                                }
                                console.log('Response: ' + JSON.stringify(response) + '\n');
                                console.log("Response@ " + new Date());
                                res.status(200).send(response);
                            }
                        });
                    }
                });
            });
        } else {
            res.status(500).send('No upload image found');
        }      
    });
    
}

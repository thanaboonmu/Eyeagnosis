// Eyeagnosis server

var config = require('./config');
var express = require('express');
var gcloud = require('google-cloud');
var PythonShell = require('python-shell');

var datastore = gcloud.datastore({
projectId: config.projectId,
keyFilename: config.keyFilename
});

var storage = gcloud.storage({
projectId: config.projectId,
keyFilename: config.keyFilename
});

var bucket = storage.bucket(config.bucketName);

var app = express();

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
    var filename = '' + new Date().getTime() + "-" + Math.random() + ".png";
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

function runPython(imageUrl, callback) {
    var options = {
      args: [imageUrl]
    };

    PythonShell.run('test.py', options, function (err, results) {
        if (err) {
            return callback(err, null);
        } else {
            console.log("Python job is done");
            console.log(results);
            callback(null, results);
        }
    });
}

app.post('/upload-image', rawBody, function (req, res) {
    
    if (req.rawBody && req.bodyLength > 0) {
        // save image to bucket
        uploadToBucket(req.rawBody, function(err, imageUrl) {
            if (err) {
                res.status(500).send(err);
            }
            else {
                console.log("Uploaded to bucket: " + imageUrl);
                // call Python backend
                runPython(imageUrl, function(err, results) {
                    var response = {};
                    if (err) {
                        res.status(500).send(err);
                    }
                    else {
                        var result = null
                        if (results && results.length > 0) {
                            result = JSON.parse(results[0].replace(new RegExp('\'', 'g'), '"'))
                        }
                        if (!result) {
                            result = {}
                        }
                        result.side = req.query.side
                        response.result = result;
                        response.status = 'OK';
                        console.log("Response: " + JSON.stringify(response) + "\n");
                        res.status(200).send(response);
                    }
                });
            }
        });
    } else {
        res.status(500).send("No upload image found");
    }

});

app.listen(8080);

console.log('Running on http://localhost:8080/');

var PythonShell = require('python-shell');

var options = {
  args: ["https://storage.googleapis.com/my-project-1479114136736.appspot.com/1516962482168-0.11378105224556578.png"]
};


PythonShell.run('test.py', options, function (err,results) {
  if (err) throw err;
  console.log(results);
  console.log('finished');
});

var config = require('./config');

var express = require('express');
var multer = require('multer')
var gcloud = require('google-cloud');

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
        if (err) return callback(err);
        callback(null, imageUrl);
      });
    });
    stream.end(imageData);
  }

app.post('/upload-image', rawBody, function (req, res) {

    if (req.rawBody && req.bodyLength > 0) {

        // TODO save image (req.rawBody) somewhere
        uploadToBucket(req.rawBody, function(err, imageUrl) {
            console.log(imageUrl);
            if (err) return callback(err);
        });
        // send some content as JSON
        res.send(200, {status: 'OK'});
    } else {
        res.send(500);
    }

});

app.post('/diagnose', rawBody, function (req, res) {

    if (req.rawBody && req.bodyLength > 0) {

        // TODO save image (req.rawBody) somewhere
        uploadToBucket(req.rawBody, function(err, imageUrl) {
            console.log(imageUrl);
            if (err) return callback(err);
        });
        // send some content as JSON
        res.send(200, {status: 'OK'});
    } else {
        res.send(500);
    }

});

app.listen(8080);

console.log('Running on http://localhost:8080/');

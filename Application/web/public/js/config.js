/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

var config = {
  apiKey: "AIzaSyCS7whf3wGGyFMNxYtvfMMiBXAzF5ykYX8",
  authDomain: "appme-booster.firebaseapp.com",
  projectId: "appme-booster",
  storageBucket: "appme-booster.appspot.com",
  messagingSenderId: "388041029257",
  appId: "1:388041029257:web:be7f9b21f36c3058bc7858",
  measurementId: "G-6H7P2CCKFG"
};
firebase.initializeApp(config);


// Google OAuth Client ID, needed to support One-tap sign-up.
// Set to null if One-tap sign-up is not supported.
var CLIENT_ID = '388041029257-nei9s7a8errmjnhbpp9r0cvm2kia2194.apps.googleusercontent.com';

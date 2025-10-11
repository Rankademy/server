#!/bin/bash
grep "versionCode =" version.gradle | awk -F\' '{ print $2 }'
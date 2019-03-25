#!/bin/bash
server_dir=$1
server_pid_file=$server_dir/vf.gf.server.pid
locator_pid_file=$server_dir/vf.gf.locator.pid
kill -9 `cat $server_pid_file`
kill -9 `cat $locator_pid_file`
rm -rf $server_pid_file
rm -rf $locator_pid_file

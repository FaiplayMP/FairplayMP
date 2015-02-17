#!/bin/bash
#run with new build
echo "./tt_sh/buildLib.sh"
file_stdout_redirected=/tmp/stdout_redirected_tt
echo "only stderr is shown here, stdout is redirected to $file_stdout_redirected"

#to skip junit test, set following to be true.
skip_junit=false

if [ "$skip_junit" == 'true' ]; then
  skip_options="$skip_options"'-Ddont.run.junit=true '
fi

ant $skip_options -Dtt_antPropertyName_YYY=value_in_commandline 1>$file_stdout_redirected
#to pass argument from commandline to junit testcase, it requires one level of redirection.


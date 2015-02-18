
echo "view list of jvm processes"
jps

echo "view thread dump of jvm processes."
echo " include live stacktrace."
javaproc_pid=3278
jstack -l $javaproc_pid

file_stdout_redirected=/tmp/stdout_redirected_tt
ant debug 1>$file_stdout_redirected

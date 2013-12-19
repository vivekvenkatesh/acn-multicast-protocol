#Scenario 1

#java controller &
#java node 4 &
#java node 5 &
#java node 6 &
#java node 9 receiver 0 &
#java node 0 sender "this is node 0 mutlicast message" &

#Scenario 2

#java controller &
#java node 4 &
#java node 5 &
#java node 9 receiver 0 &
#java node 0 sender "this is node 0 mutlicast message" &
#java node 8 receiver 0 &

#Scenario 3

java controller &
java node 4 &
java node 5 &
java node 9 receiver 0 &
java node 0 sender "this is node 0 mutlicast message" &
java node 3 &

#Scenario 4
#java controller &
#java node 4 &
#java node 5 receiver 1 &
#java node 9 receiver 0 &
#java node 0 sender "this is node 0 mutlicast message" &
#java node 3 &

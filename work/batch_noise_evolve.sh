s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=1
  cn=".1"
  l="1_1"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=5
  cn=".1"
  l="5_1"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=10
  cn=".1"
  l="10_1"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done

s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=1
  cn=".25"
  l="1_25"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=5
  cn=".25"
  l="5_25"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=10
  cn=".25"
  l="10_25"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done

s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=1
  cn=".5"
  l="1_5"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=5
  cn=".5"
  l="5_5"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
s=$(( 0 ))
for r in 1 2 3 4 5 6 7 8 9 10
do
  ts=10
  cn=".5"
  l="10_5"
  java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions 10 10 -numFoods 1 -numObstacles 30 -numObstacleTypes 4 -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r -trainingSetSize $ts -trainingCellNoise "$cn"
  x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
  s=$(( $s + $x ))
  if (( $r == 10 ))
  then
    a=$(( 100 * $s / 10 ))
    a=`echo $a | sed 's/..$/.&/'`
    echo ${ts},${cn},${a}
  fi
done
for d in 10 15 20
do
  #echo dimensions=$d
  for t in 1 2 4
  do
    #echo landmark_types=$t
    for o in 20 30 40
    do
      #echo landmarks=$o
      for f in 1 2 3
      do
        #echo foods=$f
        s=$(( 0 ))
        for r in 1 2 3 4 5 6 7 8 9 10
        do
          #echo random_seed=$r
          l="d${d}_t${t}_o${o}_f${f}_r${r}"
          java -cp ".;../lib/weka.jar" moxworx.EvolveMoxWorx -generations 10 -steps 500 -dimensions $d $d -numFoods $f -numLandmarks $o -numLandmarkTypes $t -output evolve_${l}.out -logfile evolve_${l}.log -randomSeed $r
          x=`cat evolve_${l}.log | grep -A 1 Select | tail -1 | cut -d"=" -f3 | cut -d"," -f1 | cut -d"." -f1`
          s=$(( $s + $x ))
          if (( $r == 10 ))
          then
             a=$(( 100 * $s / 10 ))
             a=`echo $a | sed 's/..$/.&/'`
             echo ${d},${t},${o},${f},${a}
          fi
        done
      done
    done
  done
done

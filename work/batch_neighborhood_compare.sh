for d in 10
do
  #echo dimensions=$d
  for t in 1 2 4
  do
    #echo landmark_types=$t
    for o in 10 20
    do
      #echo landmarks=$o
      for f in 1
      do
        #echo foods=$f
        for n in 3 2 1
        do
          #echo neighborhoods=$n
          s=$(( 0 ))
          for r in 1 2 3 4 5 6 7 8 9 10
          do
            #echo random_seed=$r
            l="d${d}_t${t}_o${o}_f${f}_${n}_r${r}"
            ./moxworx.sh -steps 200 -dimensions $d $d -numMoxen 1 -numFoods $f -numLandmarks $o -numLandmarkTypes $t -randomSeed $r -numNeighborhoods $n -driver autopilot -save run.out
            ./moxworx.sh -steps 200 -driver metamorphDB -load run.out
            if [ $? -eq 0 ]
            then
              x=1
            else
              x=0
            fi
            s=$(( $s + $x ))
            if (( $r == 10 ))
            then
              a=$(( 100 * $s / 10 ))
              a=`echo $a | sed 's/..$/.&/'`
              echo ${d},${t},${o},${f},${n},${a}
            fi
          done
        done
      done
    done
  done
done



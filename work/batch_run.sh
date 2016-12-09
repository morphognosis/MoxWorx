for d in 10 15 20
do
  #echo dimensions = $d
  for t in 1 2 4
  do
    #echo obstacle types = $t
    for o in 20 30 40
    do
      #echo obstacles = $o
      for f in 1 2 3
      do
        #echo foods = $f
        for n in 2 3 4
        do
            #echo number neighborhoods = $n
            for ns in 0 1 2
            do
                #echo neighborhood dimension stride = $ns
                for nm in 3 2 1
                do
                    #echo neighborhood dimension multiplier = $nm
                    for es in 1 2 3
                    do
                        #echo epoch stride = $es
                        for em in 3 2 1
                        do
                            #echo epoch multiplier = $em
                            for r in 1 2 3 4 5
                            do
                                #echo random seed = $r
                                c="./moxworx.sh -steps 100 -dimensions $d $d -numMoxen 1 -numFoods $f -numObstacles $o -numObstacleTypes $t -randomSeed $r -numNeighborhoods $n -neighborhoodDimensionStride $ns -neighborhoodDimensionMultiplier $nm -epochIntervalStride $es -epochIntervalMultiplier $em"
                                echo $c
                                exit 1
                            done
                        done
                    done
                done
            done
        done
      done
    done
  done
done

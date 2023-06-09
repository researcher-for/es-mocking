package org.evomaster.core.search.service

import com.google.inject.Inject
import org.evomaster.core.EMConfig
import kotlin.math.roundToInt

/**
 * Search algorithm parameters might change during the search,
 * eg based on time or fitness feedback
 */
class AdaptiveParameterControl {

    @Inject
    private lateinit var time : SearchTimeController

    @Inject
    private lateinit var config: EMConfig


    fun getExtraSqlDbConstraintsProbability() : Double {

        val p = config.useExtraSqlDbConstraintsProbability
        if(p==0.0){
            return p
        }
        if(doesFocusSearch()){
            return 1.0
        }
        return p
    }

    fun getArchiveTargetLimit() : Int {
        return getExploratoryValue(config.archiveTargetLimit, 1)
    }

    fun getProbRandomSampling(): Double {
        return getExploratoryValue(config.probOfRandomSampling , 0.0)
    }

    fun getNumberOfMutations(): Int {
        return getExploratoryValue(config.startNumberOfMutations, config.endNumberOfMutations )
    }

    fun getBaseTaintAnalysisProbability(end: Double = 0.0) : Double {
        return getExploratoryValue(config.baseTaintAnalysisProbability, end)
    }

    /**
     * Based on the current state of the search, ie how long has been passed
     * and how much budget is left before starting a focused search,
     * return  a value between [start] (at the beginning of the search) and [end]
     * (when the focused search starts)
     */
    fun getExploratoryValue(start: Int, end: Int) : Int{
        return getExploratoryValue(start.toDouble(), end.toDouble()).roundToInt()
    }

    /**
     * Based on the current state of the search, ie how long has been passed
     * and how much budget is left before starting a focused search,
     * return  a value between [start] (at the beginning of the search) and [end]
     * (when the focused search starts)
     */
    fun getExploratoryValue(start: Double, end: Double) : Double{

        return getDPCValue(start, end, 0.0, config.focusedSearchActivationTime)
    }

    /**
     * Based on the given period of the search, i.e., between [startTime] and [threshold]
     * return a scaled value between [start] (from [startTime]) and [end] (until [threshold])
     */
    fun getDPCValue(start: Double, end: Double, startTime: Double, threshold: Double) : Double{
        if (threshold < startTime)
            throw IllegalArgumentException("threshold ($threshold) should not be less than startTime ($startTime)")

        val passed: Double = time.percentageUsedBudget()
        if (passed < startTime)
            return start

        if(passed >= threshold){
            return end
        }

        val scale = (passed-startTime) / (threshold-startTime)

        val delta = end - start

        return start + (scale * delta)
    }

    /**
     * whether the search reaches the phase of 'focus search'
     */
    fun doesFocusSearch() : Boolean = time.percentageUsedBudget() >= config.focusedSearchActivationTime

}
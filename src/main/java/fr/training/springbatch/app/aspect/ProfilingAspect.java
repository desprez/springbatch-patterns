package fr.training.springbatch.app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Component
//@Aspect
public class ProfilingAspect {

	private static final Logger logger = LoggerFactory.getLogger(ProfilingAspect.class);

	@Pointcut("execution(public * fr.training.springbatch..*.*(..))")
	private void allApplicationPublicMethods() {
		// No Op
	}

	@Pointcut("execution(public * org.springframework.batch.item..*.*(..))")
	private void allSpringBatchPublicMethods() {
		// No Op
	}

	@Around("allApplicationPublicMethods() || allSpringBatchPublicMethods()")
	public Object logAround(final ProceedingJoinPoint pjp) throws Throwable {
		final long start = System.currentTimeMillis();
		try {
			return pjp.proceed();
		} finally {
			final long end = System.currentTimeMillis();
			logger.info("Method '{}' spent {} ms.", pjp.toShortString(), end - start);
		}
	}

}

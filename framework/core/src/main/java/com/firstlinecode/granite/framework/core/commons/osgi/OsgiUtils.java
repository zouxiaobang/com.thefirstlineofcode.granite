package com.firstlinecode.granite.framework.core.commons.osgi;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.IApplication;
import com.firstlinecode.granite.framework.core.platform.Equinox;
import com.firstlinecode.granite.framework.core.platform.IPlatform;
import com.firstlinecode.granite.framework.core.repository.IComponentCollector;
import com.firstlinecode.granite.framework.core.repository.IRepository;

public class OsgiUtils {
	private static IPlatform platform;
	private static final String PROPERTY_NAME_GRANITE_CONFIG_DIR = "granite.config.dir";
	private static File graniteConfigDir;

    private static final Logger logger = LoggerFactory.getLogger(OsgiUtils.class);
    
    private static Map<IContributionTracker, BundleListener> contributionListeners = new HashMap<>();

    public static String getSymbolicName(Bundle bundle) {
        String symbolicName = bundle.getSymbolicName();
        if (symbolicName == null) {
            symbolicName = "no.symbolic.name.bundle";
        }

        return symbolicName;
    }

    @SuppressWarnings("unchecked")
    public static <T> ServiceReference<T> getServiceReference(BundleContext context, Class<T> clazz, String key, String val) {
        ServiceReference<?>[] references = null;
        try {
            references = context.getServiceReferences(clazz.getName(), getEqualFilter(key, val));
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Invalid filter.", e);
        }

        if (references == null || references.length == 0) {
            return null;
        }

        return (ServiceReference<T>) references[0];
    }

    private static String getEqualFilter(String key, String val) {
		return String.format("(%s=%s)", key, val);
	}

	public static <T> T getServiceInstance(BundleContext context, Class<T> clazz, String serviceIntent) {
        ServiceReference<T> ref = getServiceReference(context, clazz, org.osgi.framework.Constants.SERVICE_INTENTS, serviceIntent);
        if (ref == null) {
            return null;
        }
        return (T) context.getService(ref);
    }
    
    public static <T> T getServiceByPID(BundleContext context, Class<T> clazz, String servicePID) {
        ServiceReference<T> ref = getServiceReference(context, clazz, org.osgi.framework.Constants.SERVICE_PID, servicePID);
        if (ref == null) {
            return null;
        }
        return (T) context.getService(ref);
    }

    public static Bundle getBundle(BundleContext bundleContext, String bundleSymblicName) {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals(bundleSymblicName)) {
                return bundle;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, Class<T> clazz) {
        ServiceReference<?> reference = bundleContext.getServiceReference(clazz.getName());
        if (reference == null) {
            return null;
        }

        return (T) bundleContext.getService(reference);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, Class<T> clazz, String key, String value) {
        ServiceReference<?> reference = getServiceReference(bundleContext, clazz, key, value);
        if (reference == null) {
            return null;
        }

        return (T) bundleContext.getService(reference);
    }

    public static <T> void trackContribution(BundleContext context, String contributionKey, IContributionTracker tracker) {
    	synchronized(contributionListeners) {
        	BundleListener listener = new ContributionBundleListener<>(contributionKey, tracker);
            context.addBundleListener(listener);
            
            contributionListeners.put(tracker, listener);
    	}
    	
        Bundle[] bundles = context.getBundles();
        for (Bundle installed : bundles) {
            if (installed.getState() == Bundle.ACTIVE) {
                findContribution(installed, contributionKey, tracker);
            }
        }
    }
    
    public static void stopTrackContribution(BundleContext bundleContext, IContributionTracker tracker) {
    	synchronized(contributionListeners) {
    		BundleListener listener = contributionListeners.remove(tracker);
    		if (listener != null) {
    			bundleContext.removeBundleListener(listener);
    		}
    	}
    }

    private static void findContribution(Bundle bundle, String contributionKey, IContributionTracker tracker) {
        Dictionary<String, String> headers = bundle.getHeaders();
        if (headers == null) {
            return;
        }

        String sContribution = headers.get(contributionKey);
        if (sContribution == null) {
            return;
        }

        try {
            tracker.found(bundle, sContribution);
        } catch (Exception e) {
        	if (logger.isWarnEnabled()) {
        		logger.warn(String.format("Can't process found contribution which's value is %s from bundle %s.",
            		sContribution, OsgiUtils.getSymbolicName(bundle)), e);
        	}
        }
    }

    private static class ContributionBundleListener<T> implements SynchronousBundleListener {

        private String contributionKey;
        private IContributionTracker tracker;

        public ContributionBundleListener(String contributionKey, IContributionTracker tracker) {
            this.contributionKey = contributionKey;
            this.tracker = tracker;

        }

        @Override
        public void bundleChanged(BundleEvent event) {
            if (event.getType() == BundleEvent.STARTED) {
                findContribution(event.getBundle(), contributionKey, tracker);
            } else if (event.getType() == BundleEvent.STOPPED) {
                lostContribution(event);
            }
        }

        private void lostContribution(BundleEvent event) {
            Dictionary<String, String> headers = event.getBundle().getHeaders();
            if (headers == null) {
                return;
            }

            String sContribution = headers.get(contributionKey);
            if (sContribution == null) {
                return;
            }

            try {
                tracker.lost(event.getBundle(), sContribution);
            } catch (Exception e) {
            	if (logger.isWarnEnabled()) {
            		logger.warn(
            			String.format("Can't process lost contribution which's value is %s from bundle %s.",
            				sContribution, OsgiUtils.getSymbolicName(event.getBundle())), e);
            	}
            }
        }
    }

	public static <T> T createInstance(Bundle bundle, String className) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        @SuppressWarnings("unchecked")
        Class<T> tClass = (Class<T>)bundle.loadClass(className);
        return tClass.newInstance();
    }
    
    public static IPlatform getPlatform(BundleContext bundleContext) {
    	if (platform == null) {
    		platform = guessPlatform(bundleContext);
    	}
    	
    	return platform;
    }

	private static IPlatform guessPlatform(BundleContext bundleContext) {
		if (bundleContext.getClass().getName().indexOf("org.eclipse") != -1) {
			return new Equinox(bundleContext);
		}
		
		throw new UnsupportedOperationException("Unspported osgi platform.");
	}
	
	public synchronized static File getGraniteConfigDir(BundleContext bundleContext) {
		if (graniteConfigDir == null) {
			String pGraniteConfigDir = System.getProperty(PROPERTY_NAME_GRANITE_CONFIG_DIR);
			
			if (pGraniteConfigDir != null) {
				graniteConfigDir = new File(pGraniteConfigDir);
				
				if (graniteConfigDir.exists() && graniteConfigDir.isDirectory()) {
					return graniteConfigDir;
				}
				
				graniteConfigDir = null;
				
				logger.error("Illegal granite configuration directory: {}.", pGraniteConfigDir);
				
				throw new IllegalArgumentException(String.format("Illegal granite configuration directory: %s.", pGraniteConfigDir));
			}
			
			IPlatform platform = getPlatform(bundleContext);
			URL platformConfigDir = platform.getConfigurationDirectory();
			if (platformConfigDir == null) {
				throw new RuntimeException("Can't determine platform configuration directory.");
			}
			
			File fPlatformConfigDir = new File(platformConfigDir.getPath());
			if (!fPlatformConfigDir.isDirectory()) {
				throw new RuntimeException(String.format("'%s' should be a directory.", fPlatformConfigDir.getPath()));
			}
			
			graniteConfigDir = new File(fPlatformConfigDir, "com.firstlinecode.granite");
		}
		
		if (graniteConfigDir.exists() && graniteConfigDir.isDirectory()) {
			return graniteConfigDir;
		}
			
		return null;
	}
	
	public static IComponentCollector getAppComponentCollector(BundleContext bundleContext) {
		return getComponentCollector(bundleContext, getAppComponentCollectorIntentsFilter());
	}
	
	public static IComponentCollector getFrameworComponentCollector(BundleContext bundleContext) {
		return getComponentCollector(bundleContext, getFrameworkComponentCollectorIntentsFilter());
	}
	
	private static IComponentCollector getComponentCollector(BundleContext bundleContext, String filter) {
		Collection<ServiceReference<IComponentCollector>> srs;
		try {
			srs = bundleContext.getServiceReferences(
					IComponentCollector.class, getAppComponentCollectorIntentsFilter());
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Invalid osgi service filter.", e);
		}
		
		if (srs != null && !srs.isEmpty()) {
			ServiceReference<IComponentCollector> sr = srs.iterator().next();
			IComponentCollector componentCollector = bundleContext.getService(sr);			
			return componentCollector;
		}
		
		return null;
	}
	
	private static String getAppComponentCollectorIntentsFilter() {
		return String.format("(%s=%s)", Constants.SERVICE_INTENTS, IApplication.GRANITE_APP_COMPONENT_COLLECTOR);
	}
	
	private static String getFrameworkComponentCollectorIntentsFilter() {
		return String.format("(%s=%s)", Constants.SERVICE_INTENTS, IRepository.GRANITE_FRAMEWORK_COMPONENT_COLLECTOR);
	}
}

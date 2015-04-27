//: gui/HPBar.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.*;
import java.lang.reflect.*;

/** Class handling the HP bar and statuses; it is a JPanel containing
 * a ShapeComponent (the HP bar itself), some JLabels (pony's name and 
 * lv) and can hold StatusLabels; the HPBar is associated with a Pony
 * instance, and automatically manages statuses, boosts and HP value via the
 * update() method, which should be called whenever status/HP change;
 * However, 'pseudo-statuses', like "Must recharge" and such extra-effects
 * must be manually set and reset with add/clearPseudoStatus().
 *
 * @author silverweed
 */
class HPBar extends JPanel {
	
	public final static int HPBAR_GRIDWIDTH = 9;

	private final static int HP_GREEN = 0x00DD60;
	private final static int HP_YELLOW = 0xFFCC00;
	private final static int HP_RED = 0xCC0000;
	private final static int HP_STROKE = 0x33AA73;
	private final static int HPBAR_LENGTH = 200;
	private final static int HPBAR_HEIGHT = 12;
	private final static int DELAY = 20;
	private final static String[] STATS = { "Atk", "Def", "SpA", "SpD", "Spe", "Eva", "Acc" };

	/** The Pony instance associated with this HPBar */
	private Pony pony;
	private Timer timer;
	/** This serves the purpose to keep the bar width constant */
	private final ShapeComponent baseBar;
	/** The Component containing the HPBar */
	private ShapeComponent hpBar;
	/** The bar itself */
	private RoundRectangle2D hpRect;
	/** The 'shadow bar' that indicates previous HP (Zarel-style) */
	private ShapeComponent hpShadow;
	private RoundRectangle2D hpShadowRect;
	private GridBagConstraints c;
	private Set<StatusLabel> statuses = Collections.synchronizedSet(new LinkedHashSet<StatusLabel>());
	private Set<PseudoStatusLabel> pseudoStatuses = Collections.synchronizedSet(new LinkedHashSet<PseudoStatusLabel>());
	private Set<GridLabel> allStatuses = Collections.synchronizedSet(new LinkedHashSet<GridLabel>());
	/** HP percentage */
	private JLabel perc = new JLabel("100%");
	private JLabel ponyName;
	private JLabel ponyLv;
	/** atk,def,spatk,spdef,speed,evasion,accuracy boosts */
	private PseudoStatusLabel[] boostLabel = new PseudoStatusLabel[7];
	/** The panel containing the status labels */
	private JPanel labelPanel = new JPanel(true);
	
	public HPBar(Pony pony,Color... txtColor) {
		setOpaque(false);

		this.pony = pony;
		// baseBar is a fixed-length bar used to determine the border; hpBar varies its length
		// according to the pony's HP.
		baseBar = new ShapeComponent(new RoundRectangle2D.Double(),Color.WHITE,0f);
		hpBar = new ShapeComponent(new RoundRectangle2D.Double(),new Color(HP_GREEN),1f);
		hpShadow = new ShapeComponent(new RoundRectangle2D.Double(),new Color(HP_GREEN),0.5f);
		hpRect = (RoundRectangle2D)hpBar.getShape();
		hpShadowRect = (RoundRectangle2D)hpShadow.getShape();

		((RoundRectangle2D)baseBar.getShape()).setRoundRect(0,0,HPBAR_LENGTH/*+45*/,HPBAR_HEIGHT,20,20);
		hpRect.setRoundRect(0,0,HPBAR_LENGTH,HPBAR_HEIGHT,20,20);
		hpShadowRect.setRoundRect(0,0,HPBAR_LENGTH,HPBAR_HEIGHT,20,20);

		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.insets = new Insets(2,1,2,1);
			
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = HPBAR_GRIDWIDTH - 2;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.8;
		c.ipadx = 3;
		ponyName = new JLabel(pony.getNickname());
		add(ponyName,c);
		
		c.gridx = HPBAR_GRIDWIDTH - 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.2;
		ponyLv = new JLabel("(Lv "+pony.getLevel()+")");
		add(ponyLv,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		c.gridwidth = HPBAR_GRIDWIDTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		// These three are overlapping
		add(hpBar,c);
		add(hpShadow,c);
		add(baseBar,c);

		c.gridx = HPBAR_GRIDWIDTH;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		add(perc,c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = HPBAR_GRIDWIDTH + 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.ipadx = 0;
		labelPanel.setLayout(new GridBagLayout());
		labelPanel.setOpaque(false);
		add(labelPanel,c);
		c.gridy = 0;
		c.insets = new Insets(0,0,0,1);
		labelPanel.add(Box.createRigidArea(new Dimension(HPBAR_LENGTH,1)),c);
		
		if(txtColor.length > 0) 
			setTextColor(txtColor[0]);
			
		update(false);
		if(Debug.on) printDebug("Constructed HPBar("+pony.getName()+", hpPerc="+perc.getText()+", status="+allStatuses+")");
	}

	public void update() {
		update(true);
	}

	 /** Updates HP value, Statuses and boosts of the pony.
	 * @param animated If true, the HP transition, if any, will be animated; else not.
	 */
	public void update(boolean animated) {
		if(Debug.on) printDebug("[HPBar] called update(). Pony statuses = "+pony.getStatus());
		// Update HP bar 
		setHp(animated);
		// Update boosts
		int i = 0;
		for(Iterator<Map.Entry<Pony.Stat,Integer>> it = pony.getVolatiles().modifiers.iterator(); it.hasNext(); ++i) {
			Map.Entry<Pony.Stat,Integer> entry = it.next();
			setBoost(i, entry.getValue());
		}

		// Remove status labels that don't afflict the pony any more
		boolean already = false;
		Iterator<StatusLabel> it = statuses.iterator();
		while(it.hasNext()) {
			StatusLabel label = it.next();
			Pony.Status status = label.getStatus();
			if(pony.getStatus() != status)
				clearStatus(status);
			else
				already = true;
		}
		
		// Then, if the pony has a status and its label isn't in the HP bar, add it (statuses
		// are mutually exclusive)
		if(pony.getStatus() != null && pony.getStatus() != Pony.Status.KO && !already)
			addStatus(pony.getStatus());
	}

	public void setTextColor(final Color color) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ponyName.setForeground(color);
				ponyLv.setForeground(color);
				perc.setForeground(color);
				validate();
				repaint();
			}
		});
	}

	/** This method removes all pseudostatuses (but NOT statuses) and shifts
	 * all statuses to cover all "holes".
	 */
	public void clearPseudoStatuses() {
		if(Debug.on) printDebug("[HPBar] Called clearPseudoStatuses(); pseudoStatuses.size = "+pseudoStatuses.size());
			if(Debug.pedantic) printDebug("run(): pseudoStatuses.size = "+pseudoStatuses.size());
			for(PseudoStatusLabel psl : pseudoStatuses) {
				if(Debug.pedantic) printDebug("removing:"+psl.getText());
				synchronized(labelPanel) {
					labelPanel.remove(psl);
				}
				allStatuses.remove(psl);
			}
			pseudoStatuses.clear();
			for(StatusLabel sl : statuses) {
				if(Debug.pedantic) printDebug("removing:"+sl);
				synchronized(labelPanel) {
					labelPanel.remove(sl);
				}
			}
			c.gridx = 0;
			for(StatusLabel sl : statuses) {
				if(Debug.pedantic) printDebug("adding: "+sl);
				addStatus(sl.getStatus());
			}
			fixGridBagConstraints();
			if(Debug.pedantic) printDebug("Ended clearPseudoStatuses()");
	}
	public void clearPseudoStatus(final String name) {
		for(PseudoStatusLabel ps : pseudoStatuses) 
			if(ps.getName().equals(name)) {
				clearLabel(ps);
				return;
			}
		if(Debug.on) printDebug("[HPBar] clearPseudoStatus("+name+"): not found in statuses.");
	}

	/** Like 'addPseudoStatus' with 3 args, but deduce gridwidth from the name
	 * length (gridWidth = name.length()/4 + 1).
	 */
	public void addPseudoStatus(final String name,final boolean good) {
		addPseudoStatus(name,good,Math.min(HPBAR_GRIDWIDTH,name.length()/4+1));
	}

	/** Adds a "pseudoStatus" to the HP bar; a pseudoStatus is a label with a
	 * custom name and gridwidth, that can be either 'good' (painted green) or
	 * 'bad' (painted red).
	 */
	public void addPseudoStatus(final String name,final boolean good,final int gridWidth) {
		if(Debug.on) printDebug("Called addPseudoStatus("+name+","+gridWidth+","+good+")");
		try {
			if(SwingUtilities.isEventDispatchThread()) {
				c.gridheight = 1;
				c.gridwidth = Math.min(HPBAR_GRIDWIDTH,gridWidth);
				c.ipadx = 3;
				c.anchor = GridBagConstraints.WEST;
				c.fill = GridBagConstraints.HORIZONTAL;
				if(c.gridx + c.gridwidth > HPBAR_GRIDWIDTH) {
					c.gridx = 0;
					++c.gridy;
				}
				PseudoStatusLabel psl = new PseudoStatusLabel(name,c.gridwidth,good,c.gridx,c.gridy);
				pseudoStatuses.add(psl);
				allStatuses.add(psl);
				// if boost label, keep the reference
				if(name.startsWith("Atk")) boostLabel[0] = psl;
				else if(name.startsWith("Def")) boostLabel[1] = psl;
				else if(name.startsWith("SpA")) boostLabel[2] = psl;
				else if(name.startsWith("SpD")) boostLabel[3] = psl;
				else if(name.startsWith("Spe")) boostLabel[4] = psl;
				else if(name.startsWith("Eva")) boostLabel[5] = psl;
				else if(name.startsWith("Acc")) boostLabel[6] = psl;
				if(Debug.on) {
					printDebug(Arrays.asList(boostLabel).toString());
				}

				synchronized(labelPanel) {
					labelPanel.add(psl,c);
				}
				if(Debug.on) printDebug("Added: "+psl);
				c.gridx += c.gridwidth;
				if(c.gridx >= HPBAR_GRIDWIDTH) {
					c.gridx = 0;
					++c.gridy;
				}
				repaint();
				if(Debug.on) printDebug("Ended addPseudoStatus("+name+",good="+good+",gridwidth="+gridWidth+")");
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						c.gridheight = 1;
						c.gridwidth = Math.min(HPBAR_GRIDWIDTH,gridWidth);
						c.ipadx = 3;
						c.anchor = GridBagConstraints.WEST;
						c.fill = GridBagConstraints.HORIZONTAL;
						if(c.gridx + c.gridwidth > HPBAR_GRIDWIDTH) {
							c.gridx = 0;
							++c.gridy;
						}
						PseudoStatusLabel psl = new PseudoStatusLabel(name,c.gridwidth,good,c.gridx,c.gridy);
						pseudoStatuses.add(psl);
						allStatuses.add(psl);
						// if boost label, keep the reference
						if(name.startsWith("Atk")) boostLabel[0] = psl;
						else if(name.startsWith("Def")) boostLabel[1] = psl;
						else if(name.startsWith("SpA")) boostLabel[2] = psl;
						else if(name.startsWith("SpD")) boostLabel[3] = psl;
						else if(name.startsWith("Spe")) boostLabel[4] = psl;
						else if(name.startsWith("Eva")) boostLabel[5] = psl;
						else if(name.startsWith("Acc")) boostLabel[6] = psl;
						if(Debug.on) {
							printDebug(Arrays.asList(boostLabel).toString());
						}

						synchronized(labelPanel) {
							labelPanel.add(psl,c);
						}
						if(Debug.on) printDebug("Added: "+psl);
						c.gridx += c.gridwidth;
						if(c.gridx >= HPBAR_GRIDWIDTH) {
							c.gridx = 0;
							++c.gridy;
						}
						repaint();
						if(Debug.on) printDebug("Ended addPseudoStatus("+name+",good="+good+",gridwidth="+gridWidth+")");
					}
				});
			}
		} catch(InterruptedException e) {
			printDebug("[HPBar.addPseudoStatus]: interrupted.");
		} catch(InvocationTargetException e) {
			printDebug("[HPBar.addPseudoStatus]: invocation target exception");
			e.printStackTrace();
		}
	}

	public void setBoost(final int statIdx, final int value) {
		if(statIdx < 0 || statIdx > STATS.length - 1) {
			printDebug("[HPBar] Error: statIdx is " + statIdx +"!");
			return;
		}

		if(value == 0) {
			// remove the label, if present
			if(boostLabel[statIdx] != null)
				clearLabel(boostLabel[statIdx]);
			return;
		}

		if(boostLabel[statIdx] != null) {
			// if stat is already boosted, just change the text
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					boostLabel[statIdx].setText(STATS[statIdx]+" "+Pony.getStatMod(value)+"x");
					boostLabel[statIdx].setGood(value >= 0);
				}
			});
		} else {
			addPseudoStatus(STATS[statIdx]+" "+Pony.getStatMod(value)+"x", value > 0, 3);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g.create();
		Stroke previousStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setColor(new Color(HP_STROKE));
		g2d.translate(baseBar.getX(),baseBar.getY());
		g2d.draw(baseBar.getShape());
		g2d.setStroke(previousStroke);
		g2d.dispose();
	}

	/** Class testing */
	public static void main(String[] args) throws Exception {
		//Debug.pedantic = true;
		JFrame f = new JFrame();
		Pony p = PonyCreator.create("Princess Celestia");
		final HPBar hp = new HPBar(p);
		JSlider slider = new JSlider(0,100,100);
		for(Pony.Status s : Pony.Status.values()) {
			if(s == Pony.Status.KO) continue;
			hp.addStatus(s);
		}
		hp.addPseudoStatus("Must recharge",false);
		hp.addPseudoStatus("Balloon",true);
		hp.setBoost(0,2);
		hp.setBoost(1,-1);
		f.add(hp);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				hp.setHp(false, ((JSlider)e.getSource()).getValue()/100f);
			}
		});
		f.add(slider,BorderLayout.SOUTH);
		SwingConsole.run(f);
		
		boolean doStuff = true;

		if(doStuff) {
			//hp.clearStatus();
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			hp.setBoost(0,-6);
			f.validate();
			f.repaint();
			hp.addStatus(Pony.Status.ASLEEP);
			hp.addPseudoStatus("Mimic",true);
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			hp.clearStatus(Pony.Status.POISONED);
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
			hp.clearStatus(Pony.Status.INTOXICATED);
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
			hp.clearStatus(Pony.Status.PETRIFIED);
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
			hp.clearPseudoStatus("Must recharge");
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
			//hp.clearStatuses();
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
			hp.addStatus(Pony.Status.BURNED);
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
			//hp.clearPseudoStatuses();
			java.util.concurrent.TimeUnit.SECONDS.sleep(1);
			f.validate();
			f.repaint();
		}
	}

	///////////////////////// PROTECTED METHODS //////////////////////////

	/** Update the HPBar length and color according to pony's hp; protected, because
	 * HP are managed by update().
	 * @param animated Whether the transition should be animated or not (default: true)
	 * @param _hpPerc The percentage of remaining HP ([0-1]) 
	 */
	protected void setHp(boolean animated, float _hpPerc) {
		if(_hpPerc < 0f) _hpPerc = 0f;
		else if(_hpPerc > 1f) _hpPerc = 1f;
		final float hpPerc = _hpPerc;

		// update shadow bar
		final float curPerc = Float.parseFloat(perc.getText().replaceAll("%","")) / 100f;
		if(curPerc <= 0.33f)
			hpShadow.setColor(new Color(HP_RED));
		else if(curPerc <= 0.5f)
			hpShadow.setColor(new Color(HP_YELLOW));
		else
			hpShadow.setColor(new Color(HP_GREEN));
		hpShadowRect.setRoundRect(0,0,Math.min(HPBAR_LENGTH,Math.max(1,curPerc*HPBAR_LENGTH)),
					hpShadowRect.getBounds().height,
					hpShadowRect.getArcWidth(),
					hpShadowRect.getArcHeight());

		if(Debug.on) printDebug("Called setHp(animated="+animated+",hpPerc="+hpPerc+")");
		if(animated) {
			timer = new Timer(DELAY,new ActionListener() {
				float initialHp = curPerc;
				float finalHp = hpPerc;
				float curHp = initialHp;
				float step = (finalHp - initialHp) / 10;
				boolean falling = finalHp < initialHp;
				boolean die;
				public void actionPerformed(ActionEvent e) {
					if(Debug.pedantic) printDebug("[anim] initialHp="+initialHp+",finalHp="+finalHp+",curHp="+curHp);
					if(die) {
						((Timer)e.getSource()).stop();
						((Timer)e.getSource()).removeActionListener(this);
						timer.stop();
						synchronized(timer) {
							timer.notifyAll();
						}
						return;
					}
					if((falling && curHp <= finalHp) || (!falling && curHp >= finalHp)) {
						die = true;	//adjust HP before suiciding
						curHp = finalHp;
					} else {
						curHp += step;
					}
					if(curHp <= 0.33f) 
						hpBar.setColor(new Color(HP_RED));
					else if(curHp <= 0.5f) 
						hpBar.setColor(new Color(HP_YELLOW));
					else
						hpBar.setColor(new Color(HP_GREEN));


					if(curHp <= 0f && finalHp <= 0f) {
						hpRect.setRoundRect(0,0,0,hpRect.getBounds().height,hpRect.getArcWidth(),hpRect.getArcHeight());
						perc.setText("0%");
					} else {
						hpRect.setRoundRect(0,0,Math.min(HPBAR_LENGTH,Math.max(1,curHp*HPBAR_LENGTH)),
								hpRect.getBounds().height,
								hpRect.getArcWidth(),
								hpRect.getArcHeight());
						perc.setText(Math.max(1,(int)(curHp*100))+"%");
					}

					repaint();
				}
			});

			timer.start();
			
		} else {
			final float width = HPBAR_LENGTH * hpPerc;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(hpPerc <= 0.33f) 
						hpBar.setColor(new Color(HP_RED));
					else if(hpPerc <= 0.5f) 
						hpBar.setColor(new Color(HP_YELLOW));
					else
						hpBar.setColor(new Color(HP_GREEN));

					if(Debug.pedantic) printDebug("Width: "+width+", Foreground: "+hpBar.getForeground());
					hpRect.setRoundRect(0,0,width,hpRect.getBounds().height,hpRect.getArcWidth(),hpRect.getArcHeight());
					if(hpPerc == 0)
						perc.setText("0%");
					else
						perc.setText(Math.max(1,(int)(hpPerc*100))+"%");
					repaint();
				}
			});
		}
	}

	protected void setHp(boolean animated) {
		setHp(animated, pony.getHpPerc());
	}

	/** Default method to set HP, which uses the pony's current hp. */
	protected void setHp() {
		setHp(true, pony.getHpPerc());
	}

	/** Adds a StatusLabel to the HP bar, appending it to the current statuses;
	 * NOTE: this method is now protected because Statuses are handled by update().
	 */
	protected void addStatus(final Pony.Status status) {
		if(Debug.on) printDebug("[HPBar] Called addStatus("+status+")");
		try {
			if(SwingUtilities.isEventDispatchThread()) {
				pony.setStatus(status, true);
				c.gridwidth = 1;
				c.gridheight = 1;
				c.ipadx = 5;
				c.anchor = GridBagConstraints.WEST;
				c.fill = GridBagConstraints.HORIZONTAL;
				if(Debug.pedantic) printDebug("c: "+c.gridx+","+c.gridy);
				StatusLabel sl = new StatusLabel(status,c.gridx,c.gridy);
				statuses.add(sl);
				allStatuses.add(sl);
				if(Debug.on) {
					printDebugnb("statuses = ");
					for(StatusLabel slb : statuses) 
						printDebugnb(slb.getText()+",");
					printDebug(" ("+statuses.size()+")");
				}

				synchronized(labelPanel) {
					labelPanel.add(sl,c);
				}
				++c.gridx;
				if(c.gridx >= HPBAR_GRIDWIDTH) {
					c.gridx = 0;
					++c.gridy;
				}
				validate();
				repaint();
				if(Debug.pedantic) printDebug("Ended addStatus("+status+")");
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						pony.setStatus(status, true);
						c.gridwidth = 1;
						c.gridheight = 1;
						c.ipadx = 5;
						c.anchor = GridBagConstraints.WEST;
						c.fill = GridBagConstraints.HORIZONTAL;
						//if(c.gridy < 2) c.gridy = 2;
						if(Debug.pedantic) printDebug("c: "+c.gridx+","+c.gridy);
						StatusLabel sl = new StatusLabel(status,c.gridx,c.gridy);
						statuses.add(sl);
						allStatuses.add(sl);
						if(Debug.on) {
							printDebugnb("statuses = ");
							for(StatusLabel slb : statuses) 
								printDebugnb(slb.getText()+",");
							printDebug(" ("+statuses.size()+")");
						}

						synchronized(labelPanel) {
							labelPanel.add(sl,c);
						}
						++c.gridx;
						if(c.gridx >= HPBAR_GRIDWIDTH) {
							c.gridx = 0;
							++c.gridy;
						}
						validate();
						repaint();
						if(Debug.pedantic) printDebug("Ended addStatus("+status+")");
					}
				});
			}
		} catch(InterruptedException e) {
			printDebug("[HPBar] addStatus("+status+") interrupted.");
		} catch(InvocationTargetException e) {
			printDebug("[HPBar] addStatus("+status+"): "+e);
			printDebug("Caused by: "+e.getCause());
		}
	}
	
	/** This method removes all statuses (but NOT pseudoStatuses) and shifts
	 * all pseudoStatuses to cover all "holes".
	 */
	protected void clearStatuses() {
		if(Debug.on) printDebug("[HPBar] Called clearStatuses(); statuses.size = "+statuses.size());
		if(Debug.pedantic) printDebug("run(): statuses.size = "+statuses.size());
		synchronized(labelPanel) {
			// remove all statuses
			for(StatusLabel sl : statuses) {
				if(Debug.pedantic) printDebug("removing:"+sl.getText());
					labelPanel.remove(sl);
				allStatuses.remove(sl);
			}
			statuses.clear();
			// remove pseudostatuses only from panel
			for(PseudoStatusLabel psl : pseudoStatuses) {
				if(Debug.pedantic) printDebug("removing:"+psl);
				labelPanel.remove(psl);
			}
			// then re-add them to cover holes
			c.gridx = 0;
			for(PseudoStatusLabel psl : pseudoStatuses) {
				if(Debug.pedantic) printDebug("adding: "+psl);
				labelPanel.add(psl, c);
				c.gridx += psl.gridwidth;
				if(c.gridx > HPBAR_GRIDWIDTH) {
					c.gridx = 0;
					++c.gridy;
				}
			}
			fixGridBagConstraints();
		}
		pony.healStatus();
		if(Debug.pedantic) printDebug("Ended clearStatuses()");
	}

	/** Removes a Status Label corresponding to 'status'; does NOT work with confusion (use
	 * clearPseudoStatus instead.
	 */
	protected void clearStatus(final Pony.Status status) {
		for(StatusLabel st : statuses) {
			if(Debug.pedantic) printDebug("[HPBar.clearStatus] found status: "+st.getStatus());
			if(st.getStatus() == status) {
				clearLabel(st);
				return;
			}
		}
		if(Debug.on) printDebug("[HPBar] clearStatus("+status+"): not found in statuses.");
	}

	/** Remove a single Status or PseudoStatus from HP bar. */
 	// To quote Linus: "Let's hope this is bug-free, 'cause this one I don't want to debug :-)"
	protected void clearLabel(final GridLabel label) {
		if(Debug.on) printDebug("[HPBar] Called clearLabel("+label+")");
		boolean found = false;
		c.gridheight = 1;
		// index of the free position in the grid. Grid position is numbered like this:
		// 0 1 2 3 4 5 6
		// 7 8 9 ...
		int free = -1;
		if(!allStatuses.contains(label)) {
			printDebug("[HPBar] Error: attempted to remove a non-existing label!");
			return;
		}
		synchronized(labelPanel) {
			labelPanel.remove(label);
		}
		free = label.pos();

		// shift all labels back
		allStatuses.remove(label);
		if(statuses.contains(label)) statuses.remove(label);
		else if(pseudoStatuses.contains(label)) pseudoStatuses.remove(label);

		outer:
		for(GridLabel gl : allStatuses) {
			if(gl.pos() > free) {
				if(Debug.pedantic) printDebug("gl: "+gl+"\npos: "+gl.pos()+"("+gl.gridx+","+gl.gridy+")"+"\nfree: "+free);
				// variable indexes
				int tmpgridx = gl.gridx;
				int tmpgridy = gl.gridy;
				// indexes of last known good position
				int goodx = gl.gridx;
				int goody = gl.gridy;
				do {
					--tmpgridx;

					if(Debug.pedantic) printDebug("tmpgridx = "+tmpgridx+" (pos: "+(tmpgridy*HPBAR_GRIDWIDTH+tmpgridx)+")");
					if(tmpgridx < 0) { // try to fill upper row
						if(Debug.pedantic) printDebug("Moved to upper row.");
						tmpgridx = HPBAR_GRIDWIDTH-1;
						--tmpgridy;
						if(tmpgridy < 0) break outer; 
					} 
					if(tmpgridx+gl.gridwidth < HPBAR_GRIDWIDTH) {
						goodx = tmpgridx;
						goody = tmpgridy;
						if(Debug.pedantic) printDebug("new good position: "+goodx+","+goody);
					}
				} while(tmpgridx+HPBAR_GRIDWIDTH*tmpgridy > free);
				/* we moved this label's origin as back as possible:
				 * check if the row has enough room for this label's
				 * width; if it has, move the label, else leave it 
				 * where it was.
				 */
				if(Debug.pedantic) printDebug("tmpgridx+gl.gridwidth="+(tmpgridx+gl.gridwidth));
				if(goodx != gl.gridx || goody != gl.gridy) {
					if(Debug.on) printDebug("clearStatus: removing "+gl);
					synchronized(labelPanel) {
						labelPanel.remove(gl);
					}
					gl.gridx = c.gridx = goodx;
					gl.gridy = c.gridy = goody;
					c.gridwidth = gl.gridwidth;
					synchronized(labelPanel) {
						labelPanel.add(gl,c);
					}
					if(Debug.on) printDebug("clearStatus: added "+gl);
				} else {
					if(Debug.on) printDebug("Not enough room for "+gl+": not moved.");
					break outer;
				}		
				free = gl.pos()+gl.gridwidth;
				if(Debug.pedantic) printDebug("new pos: "+gl.pos()+", new free: "+free);
			}
		}
		fixGridBagConstraints();
		if(Debug.on) printDebug("Ended clearStatus("+label+")");
		if(Debug.pedantic) printDebug("allStatuses: "+allStatuses);
	}
		
	
	/** This method is used to position the c.gridx and c.gridy
	 * at the right place (i.e at the end of all the statuses)
	 */
	private void fixGridBagConstraints() {
		int pos = -1;
		for(GridLabel gl : allStatuses) {
			if(gl.pos() > pos) {
				pos = gl.pos();
				c.gridy = gl.gridy;
				c.gridx = gl.gridx+gl.gridwidth;
			}
		}
		if(c.gridx >= HPBAR_GRIDWIDTH) {
			c.gridx = 0;
			++c.gridy;
		}
	}
}
